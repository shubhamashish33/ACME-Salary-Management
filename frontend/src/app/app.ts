import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient, HttpClientModule, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

type View = 'dashboard'|'employees'|'import';
interface GroupMetric { label:string; headcount:number; averageSalary:number; payroll:number }
interface Summary { headcount:number; annualPayroll:number; averageSalary:number; medianSalary:number; reportingCurrency:string; fxAsOf:string; byCountry:GroupMetric[]; byDepartment:GroupMetric[]; byLevel:GroupMetric[] }
interface Employee { id:number; employeeNumber:string; firstName:string; lastName:string; email:string; gender:string; countryCode:string; departmentId:number; jobLevelId:number; hiredOn:string; active:boolean; version:number }
interface Page<T> { content:T[]; totalElements:number; totalPages:number; number:number }
interface Salary { id:number; amount:number; currencyCode:string; effectiveFrom:string; effectiveTo:string|null }
interface ImportPreview { totalRows:number; validRows:number; invalidRows:number; errors:{row:number;field:string;message:string}[] }

@Component({selector:'app-root',imports:[CommonModule,FormsModule,HttpClientModule,DecimalPipe,DatePipe],templateUrl:'./app.html',styleUrl:'./app.scss'})
export class App {
  api = new URLSearchParams(location.search).get('apiUrl') || (window as any).__SALARY_API_URL__ || 'http://localhost:8080';
  view=signal<View>('dashboard'); loggedIn=signal(false); loading=signal(false); error=signal('');
  username='hr@acme.test'; password='ChangeMe123!'; summary=signal<Summary|null>(null);
  employees=signal<Page<Employee>|null>(null); selected=signal<Employee|null>(null); salaries=signal<Salary[]>([]);
  editing=signal(false); employeeDraft:any={};
  query=''; active='true'; page=0; amount?:number; currency='USD'; effectiveFrom=new Date().toISOString().slice(0,10);
  file?:File; preview=signal<ImportPreview|null>(null); importing=signal(false);
  auth=computed(()=>new HttpHeaders({Authorization:'Basic '+btoa(`${this.username}:${this.password}`)}));
  constructor(private http:HttpClient) { const saved=sessionStorage.getItem('salary-auth'); if(saved){[this.username,this.password]=JSON.parse(saved);this.login();} }
  private options(){return {headers:this.auth()};}
  login(){this.loading.set(true);this.error.set('');this.http.get(`${this.api}/api/v1/auth/me`,this.options()).subscribe({next:()=>{sessionStorage.setItem('salary-auth',JSON.stringify([this.username,this.password]));this.loggedIn.set(true);this.loading.set(false);this.loadDashboard();},error:e=>this.fail(e,'Unable to sign in. Check the demo credentials.')});}
  logout(){sessionStorage.removeItem('salary-auth');this.loggedIn.set(false);this.summary.set(null);}
  navigate(v:View){this.view.set(v);this.error.set('');if(v==='dashboard')this.loadDashboard();if(v==='employees')this.loadEmployees();}
  loadDashboard(){this.loading.set(true);this.http.get<Summary>(`${this.api}/api/v1/analytics/summary`,this.options()).subscribe({next:v=>{this.summary.set(v);this.loading.set(false);},error:e=>this.fail(e,'The free API may be waking up. Please retry in about a minute.')});}
  loadEmployees(page=this.page){this.loading.set(true);this.page=page;const params:any={page,size:25,sort:'lastName,asc',active:this.active};if(this.query)params.q=this.query;this.http.get<Page<Employee>>(`${this.api}/api/v1/employees`,{...this.options(),params}).subscribe({next:v=>{this.employees.set(v);this.loading.set(false);},error:e=>this.fail(e,'Could not load employees.')});}
  selectEmployee(e:Employee){this.selected.set(e);this.editing.set(false);this.employeeDraft={...e};this.amount=undefined;this.http.get<Salary[]>(`${this.api}/api/v1/employees/${e.id}/salaries`,this.options()).subscribe({next:v=>this.salaries.set(v),error:x=>this.fail(x,'Could not load salary history.')});}
  openCreate(){this.selected.set(null);this.employeeDraft={employeeNumber:'',firstName:'',lastName:'',email:'',gender:'Prefer not to say',countryCode:'US',departmentId:1,jobLevelId:1,hiredOn:new Date().toISOString().slice(0,10)};this.editing.set(true);}
  saveEmployee(){const current=this.selected();const request=current?this.http.put<Employee>(`${this.api}/api/v1/employees/${current.id}`,this.employeeDraft,this.options()):this.http.post<Employee>(`${this.api}/api/v1/employees`,this.employeeDraft,this.options());request.subscribe({next:e=>{this.editing.set(false);this.loadEmployees(0);this.selectEmployee(e);},error:x=>this.fail(x,'Employee was not saved.')});}
  addSalary(){const e=this.selected();if(!e||!this.amount)return;this.http.post<Salary>(`${this.api}/api/v1/employees/${e.id}/salaries`,{amount:this.amount,currencyCode:this.currency,effectiveFrom:this.effectiveFrom},this.options()).subscribe({next:()=>this.selectEmployee(e),error:x=>this.fail(x,'Salary change was not saved.')});}
  deactivate(e:Employee){if(!confirm(`Deactivate ${e.firstName} ${e.lastName}?`))return;this.http.delete(`${this.api}/api/v1/employees/${e.id}`,this.options()).subscribe({next:()=>{this.selected.set(null);this.loadEmployees();},error:x=>this.fail(x,'Employee was not deactivated.')});}
  chooseFile(event:Event){this.file=(event.target as HTMLInputElement).files?.[0];this.preview.set(null);}
  validateFile(){if(!this.file)return;const data=new FormData();data.append('file',this.file);this.loading.set(true);this.http.post<ImportPreview>(`${this.api}/api/v1/imports/validate`,data,this.options()).subscribe({next:v=>{this.preview.set(v);this.loading.set(false);},error:x=>this.fail(x,'CSV validation failed.')});}
  importFile(){if(!this.file||this.preview()?.invalidRows)return;const data=new FormData();data.append('file',this.file);this.importing.set(true);this.http.post<ImportPreview>(`${this.api}/api/v1/imports/employees`,data,this.options()).subscribe({next:v=>{this.preview.set(v);this.importing.set(false);},error:x=>{this.importing.set(false);this.fail(x,'CSV import failed. No rows were saved.');}});}
  exportCsv(){this.http.get(`${this.api}/api/v1/employees/export`,{...this.options(),responseType:'blob'}).subscribe(blob=>{const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='employees.csv';a.click();URL.revokeObjectURL(a.href);});}
  downloadTemplate(){this.http.get(`${this.api}/api/v1/imports/template`,{...this.options(),responseType:'blob'}).subscribe(blob=>{const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='employees-template.csv';a.click();URL.revokeObjectURL(a.href);});}
  formatMoney(value:number,currency='USD'){return new Intl.NumberFormat('en-US',{style:'currency',currency,maximumFractionDigits:0}).format(value||0);}
  private fail(error:HttpErrorResponse,message:string){this.loading.set(false);this.error.set(error.error?.message||message);}
}

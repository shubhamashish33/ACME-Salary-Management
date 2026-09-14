import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EmployeeDirectoryComponent } from './employee-directory/employee-directory.component';
import { IconComponent } from './icon.component';
import { ImportPreview, Summary } from './models';
import { OverviewComponent } from './overview/overview.component';

type View = 'dashboard'|'employees'|'import';

@Component({selector:'app-root',imports:[CommonModule,FormsModule,HttpClientModule,OverviewComponent,EmployeeDirectoryComponent,IconComponent],templateUrl:'./app.html',styleUrl:'./app.scss'})
export class App {
  api = new URLSearchParams(location.search).get('apiUrl') || (window as any).__SALARY_API_URL__ || 'http://localhost:8080';
  view=signal<View>('dashboard'); loggedIn=signal(false); loading=signal(false); error=signal('');
  username='hr@acme.test'; password=''; summary=signal<Summary|null>(null);
  file?:File; preview=signal<ImportPreview|null>(null); importing=signal(false);
  auth=computed(()=>new HttpHeaders({Authorization:'Basic '+btoa(`${this.username}:${this.password}`)}));
  constructor(private http:HttpClient) { const saved=sessionStorage.getItem('salary-auth'); if(saved){[this.username,this.password]=JSON.parse(saved);this.login();} }
  private options(){return {headers:this.auth()};}
  login(){this.loading.set(true);this.error.set('');this.http.get(`${this.api}/api/v1/auth/me`,this.options()).subscribe({next:()=>{sessionStorage.setItem('salary-auth',JSON.stringify([this.username,this.password]));this.loggedIn.set(true);this.loading.set(false);this.loadDashboard();},error:e=>this.fail(e,'Unable to sign in. Check the demo credentials.')});}
  logout(){sessionStorage.removeItem('salary-auth');this.loggedIn.set(false);this.summary.set(null);}
  navigate(v:View){this.view.set(v);this.error.set('');if(v==='dashboard')this.loadDashboard();}
  loadDashboard(){this.loading.set(true);this.http.get<Summary>(`${this.api}/api/v1/analytics/summary`,this.options()).subscribe({next:v=>{this.summary.set(v);this.loading.set(false);},error:e=>this.fail(e,'The free API may be waking up. Please retry in about a minute.')});}
  chooseFile(event:Event){this.file=(event.target as HTMLInputElement).files?.[0];this.preview.set(null);}
  validateFile(){if(!this.file)return;const data=new FormData();data.append('file',this.file);this.loading.set(true);this.http.post<ImportPreview>(`${this.api}/api/v1/imports/validate`,data,this.options()).subscribe({next:v=>{this.preview.set(v);this.loading.set(false);},error:x=>this.fail(x,'CSV validation failed.')});}
  importFile(){if(!this.file||this.preview()?.invalidRows)return;const data=new FormData();data.append('file',this.file);this.importing.set(true);this.http.post<ImportPreview>(`${this.api}/api/v1/imports/employees`,data,this.options()).subscribe({next:v=>{this.preview.set(v);this.importing.set(false);},error:x=>{this.importing.set(false);this.fail(x,'CSV import failed. No rows were saved.');}});}
  downloadTemplate(){this.http.get(`${this.api}/api/v1/imports/template`,{...this.options(),responseType:'blob'}).subscribe(blob=>{const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='employees-template.csv';a.click();URL.revokeObjectURL(a.href);});}
  private fail(error:HttpErrorResponse,message:string){this.loading.set(false);this.error.set(error.error?.message||message);}
}

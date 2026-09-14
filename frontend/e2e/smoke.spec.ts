import { expect, test } from '@playwright/test';
test('HR can sign in and reach compensation overview',async({page})=>{
 await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{username:'hr@acme.test',role:'HR_MANAGER'}}));
 await page.route('**/api/v1/analytics/summary',r=>r.fulfill({json:{headcount:10000,annualPayroll:840000000,averageSalary:84000,medianSalary:79000,reportingCurrency:'USD',fxAsOf:'2026-01-01',byCountry:[],byDepartment:[],byLevel:[]}}));
 await page.goto('/'); await page.getByRole('button',{name:'Sign in'}).click();
 await expect(page.getByRole('heading',{name:'Compensation overview'})).toBeVisible();
 await expect(page.getByText('10,000').first()).toBeVisible();
});

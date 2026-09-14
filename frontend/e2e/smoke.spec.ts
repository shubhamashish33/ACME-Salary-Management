import { expect, Page, test } from '@playwright/test';

const summary = {
  headcount: 10000,
  annualPayroll: 840000000,
  averageSalary: 84000,
  medianSalary: 79000,
  reportingCurrency: 'USD',
  fxAsOf: '2026-01-01',
  byCountry: [{ label: 'United States', headcount: 4000, averageSalary: 105000, payroll: 420000000 }],
  byDepartment: [{ label: 'Engineering', headcount: 2500, averageSalary: 99000, payroll: 247500000 }],
  byLevel: [],
};

async function mockApi(page: Page): Promise<void> {
  await page.route('**/api/v1/auth/me', (route) => route.fulfill({ json: { username: 'hr@acme.test', role: 'HR_MANAGER' } }));
  await page.route('**/api/v1/analytics/summary', (route) => route.fulfill({ json: summary }));
  await page.route('**/api/v1/reference/countries', (route) => route.fulfill({ json: [{ value: 'US', label: 'United States' }] }));
  await page.route('**/api/v1/reference/departments', (route) => route.fulfill({ json: [{ value: '2', label: 'Engineering' }] }));
  await page.route('**/api/v1/reference/levels', (route) => route.fulfill({ json: [{ value: '3', label: 'Senior' }] }));
  await page.route('**/api/v1/employees?**', (route) => route.fulfill({ json: {
    content: [{ id: 1, employeeNumber: 'ACME-00001', firstName: 'Avery', lastName: 'Patel', email: 'avery@acme.test', gender: 'Prefer not to say', countryCode: 'US', departmentId: 2, jobLevelId: 3, hiredOn: '2025-01-01', active: true, version: 0 }],
    totalElements: 1, totalPages: 1, number: 0,
  } }));
}

async function signIn(page: Page): Promise<void> {
  await page.goto('/');
  await page.getByLabel('Password').fill('local-test-password');
  await page.getByRole('button', { name: 'Sign in' }).click();
}

test('HR can sign in and reach the compensation overview', async ({ page }) => {
  await mockApi(page);
  await signIn(page);
  await expect(page.getByRole('heading', { name: 'Compensation overview' })).toBeVisible();
  await expect(page.getByText('10,000').first()).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Average salary by country' })).toBeVisible();
});

test('employee form uses understandable reference options', async ({ page }) => {
  await mockApi(page);
  await signIn(page);
  await page.getByRole('button', { name: 'Employees' }).click();
  await expect(page.getByText('Engineering').first()).toBeVisible();
  await page.getByRole('button', { name: 'New employee' }).click();
  await expect(page.getByLabel('Department').locator('option:checked')).toHaveText('Engineering');
  await expect(page.getByLabel('Job level').locator('option:checked')).toHaveText('Senior');
});

test('employee directory fits a phone viewport without horizontal overflow', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await mockApi(page);
  await signIn(page);
  await page.getByRole('button', { name: 'Employees' }).click();
  await expect(page.getByRole('navigation', { name: 'Primary navigation' })).toBeVisible();
  await expect(page.getByText('Avery Patel')).toBeVisible();
  const overflowingElements = await page.evaluate(() => [...document.querySelectorAll<HTMLElement>('body *')]
    .filter((element) => element.getBoundingClientRect().right > window.innerWidth + 1)
    .map((element) => ({ element: `${element.tagName.toLowerCase()}.${element.className}`, name: element.getAttribute('aria-label') ?? element.getAttribute('placeholder'), right: element.getBoundingClientRect().right, width: element.getBoundingClientRect().width, parent: element.parentElement?.className })));
  expect(overflowingElements).toEqual([]);
  const sidebarPosition = await page.locator('.sidebar').evaluate((element) => getComputedStyle(element).position);
  expect(sidebarPosition).toBe('fixed');
});

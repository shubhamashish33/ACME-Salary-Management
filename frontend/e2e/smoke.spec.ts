import { expect, Page, test } from '@playwright/test';

const summary = {
  headcount: 10000,
  annualPayroll: 840000000,
  averageSalary: 84000,
  medianSalary: 79000,
  reportingCurrency: 'USD',
  fxAsOf: '2026-01-01',
  salaryDistribution: { minimum: 42000, percentile25: 65000, median: 79000, percentile75: 102000, maximum: 180000 },
  byCountry: [{ label: 'United States', headcount: 4000, averageSalary: 105000, payroll: 420000000 }],
  byDepartment: [{ label: 'Engineering', headcount: 2500, averageSalary: 99000, payroll: 247500000 }],
  byLevel: [{ label: 'Senior', headcount: 2000, averageSalary: 97000, payroll: 194000000 }],
  byGender: [{ label: 'Female', headcount: 3400, averageSalary: 83000, payroll: 282200000 }],
};

async function mockApi(page: Page, employeeActive = true): Promise<void> {
  await page.route('**/api/v1/auth/me', (route) => route.fulfill({ json: { username: 'hr@acme.test', role: 'HR_MANAGER' } }));
  await page.route('**/api/v1/analytics/summary**', (route) => route.fulfill({ json: summary }));
  await page.route('**/api/v1/reference/countries', (route) => route.fulfill({ json: [{ value: 'US', label: 'United States' }] }));
  await page.route('**/api/v1/reference/departments', (route) => route.fulfill({ json: [{ value: '2', label: 'Engineering' }] }));
  await page.route('**/api/v1/reference/levels', (route) => route.fulfill({ json: [{ value: '3', label: 'Senior' }] }));
  await page.route('**/api/v1/employees/1/salaries', (route) => route.fulfill({ json: [{ id: 1, amount: 100000, currencyCode: 'USD', effectiveFrom: '2026-09-14', effectiveTo: null }] }));
  await page.route('**/api/v1/employees?**', (route) => route.fulfill({ json: {
    content: [{ id: 1, employeeNumber: 'ACME-00001', firstName: 'Avery', lastName: 'Patel', email: 'avery@acme.test', gender: 'Prefer not to say', countryCode: 'US', departmentId: 2, jobLevelId: 3, hiredOn: '2025-01-01', active: employeeActive, version: 0 }],
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
  await expect(page.getByRole('heading', { name: 'Pay by job level' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Pay by gender' })).toBeVisible();
  await expect(page.getByText('25th percentile')).toBeVisible();
});

test('dashboard filters are applied and represented in the URL', async ({ page }) => {
  await mockApi(page);
  await signIn(page);
  await page.getByLabel('Department').selectOption('2');
  await page.getByLabel('Gender').selectOption('Female');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await expect(page).toHaveURL(/departmentId=2/);
  await expect(page).toHaveURL(/gender=Female/);
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

test('inactive employee profile does not offer deactivation again', async ({ page }) => {
  await mockApi(page, false);
  await signIn(page);
  await page.getByRole('button', { name: 'Employees' }).click();
  await page.getByLabel('Employee status').selectOption('false');
  await page.getByText('Avery Patel').click();
  await expect(page.getByText('Employee is inactive')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Deactivate employee' })).toHaveCount(0);
});

test('profile and create form open as overlay cards on a phone', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await mockApi(page);
  await signIn(page);
  await page.getByRole('button', { name: 'Employees' }).click();

  await page.getByText('Avery Patel').click();
  const profileCard = page.locator('.detail');
  await expect(profileCard).toBeVisible();
  await expect(page.getByRole('button', { name: 'Close profile overlay' })).toBeVisible();
  expect(await profileCard.evaluate((element) => getComputedStyle(element).position)).toBe('fixed');

  await page.getByRole('button', { name: 'Close profile', exact: true }).click();
  await page.getByRole('button', { name: 'New employee' }).click();
  await expect(page.getByRole('heading', { name: 'Create profile' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Close form overlay' })).toBeVisible();
  expect(await page.locator('.detail').evaluate((element) => getComputedStyle(element).position)).toBe('fixed');
});

test('employee details open as an overlay on a compact desktop', async ({ page }) => {
  await page.setViewportSize({ width: 1200, height: 800 });
  await mockApi(page);
  await signIn(page);
  await page.getByRole('button', { name: 'Employees' }).click();
  await page.getByText('Avery Patel').click();

  const detailCard = page.locator('.detail');
  await expect(detailCard).toBeVisible();
  expect(await detailCard.evaluate((element) => getComputedStyle(element).position)).toBe('fixed');
  await expect(page.getByText('Effective Sep 14, 2026 — Current')).toBeVisible();

  await page.getByRole('button', { name: 'Close profile', exact: true }).click();
  await page.getByRole('button', { name: 'New employee' }).click();
  await expect(page.getByRole('heading', { name: 'Create profile' })).toBeVisible();
  expect(await page.locator('.detail').evaluate((element) => getComputedStyle(element).position)).toBe('fixed');
});

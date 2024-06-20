import { expect, test } from '@playwright/test';

test('home page has expected div with text home', async ({ page }) => {
	await page.goto('/');
	await expect(page.getByTestId('home-element')).toBeVisible();
});

test('login page has expected form', async ({ page }) => {
	await page.goto('/login');
	await expect(page.locator('form')).toBeVisible();
});

test('register page has expected form', async ({ page }) => {
	await page.goto('/register');
	await expect(page.locator('form')).toBeVisible();
});

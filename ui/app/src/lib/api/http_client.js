import { localStorageStore } from '$lib/store';

/**
 * @typedef {{redirect:string|null, errors: Array<{field: string|null, message: string}>}} ApiResponse
 */

/**
 * Login to the application.
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<ApiResponse>}
 */
async function login(username, password) {
	console.log('Logging in with username:', username, 'and password:', password);

	const resp = await fetch('http://localhost:8080/api/login', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ username, password })
	});

	return handleResponse(resp, '/');
}

/**
 * Logout from the application.
 *
 * @returns {void}
 */
function logout() {
	localStorageStore.clear('auth');
}

/**
 * Register a new user.
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<ApiResponse>}
 */
async function register(username, password) {
	console.log('Registering with username:', username, 'and password :', password);

	const access = {
		group: 'ui',
		role: 'user',
		permissions: ['read', 'write']
	};

	const resp = await fetch('http://localhost:8080/api/register', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ username, password, access })
	});

	return handleResponse(resp, '/login');
}

/**
 * @param {Response} resp
 * @param {string} redirect
 * @returns {Promise<ApiResponse>}
 */
async function handleResponse(resp, redirect) {
	console.log('resp.status', resp.status);

	if (resp.ok) {
		if (resp.status === 204) {
			console.log('success no content');
			return {
				redirect: redirect,
				errors: []
			};
		}

		const json = await resp.json();
		console.log('success json', json);
		localStorageStore.set('auth', json);
		return {
			redirect: redirect,
			errors: []
		};
	}

	if (resp.status >= 400 && resp.status < 500) {
		const json = await resp.json();
		console.log('client error json', json);
		return {
			redirect: null,
			errors: json.errors
    };
	}

	if (resp.status >= 500) {
		console.log('server error');
		return {
			redirect: null,
			errors: [
				{
					field: null,
					message: 'Server error'
				}
			]
		};
	}
}

export { login, logout, register };

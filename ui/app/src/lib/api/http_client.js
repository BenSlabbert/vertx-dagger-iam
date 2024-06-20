/**
 * @typedef {{redirect:string|null, errors: Array<{field: string|null, message: string}>}} Response
 */

/**
 * Login to the application.
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<Response>}
 */
function login(username, password) {
	console.log('Logging in with username:', username, 'and password:', password);

	// const isValid = new Date().getTime() % 2 === 0;
	const isValid = false;

	return Promise.resolve({
		redirect: isValid ? '/' : null,
		errors: isValid
			? []
			: [
					{
						field: 'username',
						message: 'Invalid username'
					},
					{
						message: 'Global form error'
					}
				]
	});
}

/**
 * Register a new user.
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<Response>}
 */
function register(username, password) {
	console.log('Registering with username:', username, 'and password :', password);

	const isValid = new Date().getTime() % 2 === 0;

	return Promise.resolve({
		redirect: isValid ? '/login' : null,
		errors: isValid
			? []
			: [
					{
						field: 'username',
						message: 'Invalid username'
					}
				]
	});
}

export { login, register };

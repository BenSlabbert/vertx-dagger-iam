/**
 * @param {string} key
 * @return {object|null} value
 */
function get(key) {
	return JSON.parse(localStorage.getItem(key));
}

/**
 * @param {string} key
 * @param {object} value
 * @return {void}
 */
function set(key, value) {
	localStorage.setItem(key, JSON.stringify(value));
}

/**
 * @param {string} key
 * @return {void}
 */
function clear(key) {
	localStorage.removeItem(key);
}

export { get, set, clear };

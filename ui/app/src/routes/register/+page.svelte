<script lang="js">
	import api from '$lib/api';

	/**
	 * @type {string}
	 */
	let username;

	/**
	 * @type {string}
	 */
	let password;

	function handleRegisterCLick() {
		api.register(username, password);
	}
</script>

<form data-testid="register-form">
	<fieldset>
		<label>
			Username
			<input
				type="text"
				name="username"
				placeholder="Username"
				autocomplete="username"
				bind:value={username}
			/>
		</label>
		<label>
			Password
			<input
				type="password"
				name="password"
				placeholder="Password"
				autocomplete="current-password"
				bind:value={password}
			/>
		</label>
	</fieldset>

	<input type="submit" value="Register" on:click|preventDefault={handleRegisterCLick} />
</form>

<div>
	<span>Already have an account? <a href="/login">Login here</a></span>
</div>

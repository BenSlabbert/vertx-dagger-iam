<script lang="js">
	import api from '$lib/api';
	import { goto } from '$app/navigation';

	/**
	 * @type {string}
	 */
	let username;

	/**
	 * @type {string}
	 */
	let password;

	/**
	 * @type {boolean}
	 */
	let isLoading = false;

	/**
	 * @type {Record<string, string>}
	 */
	let fieldErrors = {};

	/**
	 * @type {Array<string>}
	 */
	let formErrors = [];

	function resetForm() {
		username = '';
		password = '';
		fieldErrors = {};
		formErrors = [];
	}

	async function handleLoginCLick() {
		isLoading = true;
		const { redirect, errors } = await api.login(username, password);

		fieldErrors = {};
		formErrors = [];

		if (redirect) {
			await goto(redirect);
			return;
		}

		errors.forEach((e) => {
			if (e.field) {
				fieldErrors[e.field] = e.message;
			} else {
				formErrors.push(e.message);
			}
		});

		isLoading = false;
	}
</script>

<form class="container" data-testid="login-form" style="width: 50%">
	<fieldset disabled={isLoading}>
		<label>
			<span style="color: var(--pico-primary)">*</span>Username
			<input
				required
				aria-invalid={fieldErrors['username'] ? 'true' : null}
				type="text"
				name="username"
				placeholder="Username"
				autocomplete="username"
				bind:value={username}
			/>
			{#if fieldErrors['username']}
				<small>{fieldErrors['username']}</small>
			{/if}
		</label>
		<label>
			<span style="color: var(--pico-primary)">*</span>Password
			<input
				required
				aria-invalid={fieldErrors['password'] ? 'true' : null}
				type="password"
				name="password"
				placeholder="Password"
				autocomplete="current-password"
				bind:value={password}
			/>
			{#if fieldErrors['password']}
				<small>{fieldErrors['password']}</small>
			{/if}
		</label>
	</fieldset>

	{#if formErrors.length > 0}
		{#each formErrors as error}
			<div style="margin-bottom: calc(var(--pico-spacing)* .375); color: var(--pico-del-color)">
				<span>{error}</span>
			</div>
		{/each}
	{/if}

	<div class="grid">
		<button aria-busy={isLoading} type="submit" on:click|preventDefault={handleLoginCLick}>
			Login
		</button>
		<button
			class="outline secondary"
			disabled={isLoading}
			type="submit"
			on:click|preventDefault={resetForm}>Reset</button
		>
	</div>
	<span>New here? <a href="/register">Register here</a></span>
</form>

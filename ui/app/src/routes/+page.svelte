<script>
	import api from '$lib/api';
	import { goto } from '$app/navigation';

	function logout() {
		api.logout();
		goto('/login');
	}
</script>

<nav>
	<button on:click|preventDefault={logout} data-testid="logout-button" class="outline secondary">
		Log out
	</button>
</nav>

<div data-testid="home-element">home</div>

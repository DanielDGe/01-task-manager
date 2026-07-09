import keycloak from './keycloak';

export async function apiFetch(url, options = {}) {
  try {
    await keycloak.updateToken(30);
  } catch {
    await keycloak.login();
    throw new Error('Authentication session expired');
  }

  return fetch(url, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Bearer ${keycloak.token}`
    }
  });
}
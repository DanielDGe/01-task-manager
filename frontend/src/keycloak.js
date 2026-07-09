import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'task-manager',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'task-manager-web'
});

export default keycloak;
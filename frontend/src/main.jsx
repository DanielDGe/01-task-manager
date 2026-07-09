import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import keycloak from './keycloak.js';

async function bootstrap() {
  try {
    const authenticated = await keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
      pkceMethod: 'S256'
    });

    if (!authenticated) {
      await keycloak.login();
      return;
    }

    createRoot(document.getElementById('root')).render(
      <StrictMode>
        <App keycloak={keycloak} />
      </StrictMode>
    );
  } catch (error) {
    console.error('Keycloak initialization failed', error);
  }
}

bootstrap();
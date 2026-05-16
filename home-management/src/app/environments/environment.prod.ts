export const environment = {
    production: true,
    // Im Docker-Container läuft Backend auf Port 8080
    // Nginx leitet /api/* Anfragen weiter – oder direkte URL
    apiUrl: 'http://localhost:8080',
};
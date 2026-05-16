FROM nginx:alpine

# Angular Routing fix – direkt im Dockerfile, keine externe nginx.conf nötig
RUN echo 'server { \
    listen 80; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { try_files $uri $uri/ /index.html; } \
}' > /etc/nginx/conf.d/default.conf

# Lokal gebaute Angular-App kopieren
# Vorher ausführen: ng build --configuration production
COPY dist/home-management /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
#!/bin/sh
set -e

# Process nginx template with environment variables (runs as root)
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Ensure correct permissions
chown -R nginx:nginx /usr/share/nginx/html
chown -R nginx:nginx /var/cache/nginx
chown -R nginx:nginx /var/log/nginx
touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid

# Start nginx (nginx will run as nginx user)
exec nginx -g "daemon off;"

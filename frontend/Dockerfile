FROM node:24-alpine3.22 as build


WORKDIR /app

# Build Angular for production
# first install angular (so it can be cached)
RUN npm install -g @angular/cli

COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the app
COPY . .

# build for production
RUN ng build --configuration=production


FROM nginx:1.29.5


# Copy custom HTML / static files into Nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

COPY default.conf /etc/nginx/conf.d/default.conf

# Default command runs Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]

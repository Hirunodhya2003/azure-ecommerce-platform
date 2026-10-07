# E-commerce frontend (React + Vite)

## Run
    npm install
    npm run dev        # http://localhost:5173  (proxies /api -> http://localhost:8080)

## Production
    cp .env.example .env   # set VITE_API_URL=https://your-api-host/api
    npm run build          # output in dist/

The first ADMIN must be created directly in the database (register always creates CUSTOMER).

# INCLOVE Frontend Console

This Vite + Vue 3 application is a lightweight console for working with the INCLOVE backend. It implements:

- Email + user ID registration and login flows that call `/auth/register` and `/auth/login`.
- Conversation bootstrap via `/conversation` (POST) and `/conversation/userIdAndPeerUserId` (GET).
- Real-time chat using the backend's STOMP WebSocket gateway (`/ws`), including message acknowledgements and presence heartbeats.

## Getting Started

```bash
cd frontend
npm install
npm run dev
```

The dev server defaults to <http://localhost:5173>. Update the REST / WebSocket base URLs in the UI to match your backend deployment.

## Production Build

```bash
npm run build
```

The build output is generated in `frontend/dist` (ignored by git). Serve the static assets with any HTTP server to deploy the console.

## Environment Notes

- STOMP traffic is transported through SockJS, so the WebSocket base URL should use `http(s)` rather than `ws(s)` (SockJS upgrades internally).
- All requests expect a Bearer JWT issued by the backend; the UI stores the token in `localStorage` for convenience.

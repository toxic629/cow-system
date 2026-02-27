import { http, unwrap } from './http';

type LoginData = {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
};

export async function login(username: string, password: string) {
  const data = await unwrap<LoginData>(
    http.post('/api/auth/login', { username, password })
  );
  return `${data.tokenType} ${data.token}`;
}

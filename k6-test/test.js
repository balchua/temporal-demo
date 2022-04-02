import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  http.post('http://localhost:8066/api/v1/doAction');
  sleep(1);
}

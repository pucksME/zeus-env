import express from 'express';
import {name, address, port} from './routes';

export const app = express();
app.listen(port, () => console.log(`Server "${name}" is running @ ${address}:${port}`));

import express from 'express';
import {name, port} from './routes.ts';

const app = express();
app.listen(port, () => console.log(`Server "${name} is running on port "${port}"`));

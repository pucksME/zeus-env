import express from 'express';
import bodyParser from 'body-parser';
import {name, address, port} from './configuration';

export const app = express();

app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

app.listen(port, () => console.log(`Server "${name}" is running @ ${address}:${port}`));

import express from 'express';
import ViteExpress from 'vite-express';
import cors from 'cors';
import 'dotenv/config';
import router from './router.js';

const app = express();
app.use(cors());
app.use(express.json());
app.use('/api', router);

const port = process.env.NODE_ENV === 'production' ? 80 : 8008
ViteExpress.listen(app, port, () =>
  console.log(`Server is listening on port ${port}...`),
);


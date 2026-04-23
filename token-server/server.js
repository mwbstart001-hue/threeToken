require('dotenv').config();
const express = require('express');
const jwt = require('jsonwebtoken');

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'default-secret';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '24h';

// 生成 Token
app.post('/api/token/generate', (req, res) => {
  const { userId, username, role } = req.body;

  if (!userId || !username) {
    return res.status(400).json({
      error: '缺少必要参数：userId 和 username'
    });
  }

  const payload = {
    userId,
    username,
    role: role || 'user',
    iat: Math.floor(Date.now() / 1000)
  };

  const token = jwt.sign(payload, JWT_SECRET, {
    expiresIn: JWT_EXPIRES_IN
  });

  res.json({
    success: true,
    token,
    expiresIn: JWT_EXPIRES_IN
  });
});

// 验证 Token
app.post('/api/token/verify', (req, res) => {
  const { token } = req.body;

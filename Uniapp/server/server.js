const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const bodyParser = require('body-parser');
const axios = require('axios');
const MCPToolManager = require('./tools/mcp-manager.js');
require('dotenv').config();

// 初始化 MCP 工具管理器
const mcpManager = new MCPToolManager();

// 敏感词库
const SENSITIVE_WORDS = [
  // 政治敏感
  '政治敏感词', '政府', '国家领导人',
  // 色情低俗
  '色情', '淫秽', '裸体', '成人',
  // 暴力恐怖
  '暴力', '恐怖', '杀人', '爆炸',
  // 广告营销
  '加微信', '加 QQ', '刷单', '兼职', '赚钱', '投资', '理财', '贷款', '信用卡',
  // 辱骂诽谤
  '傻逼', 'sb', '垃圾', '废物', '蠢货',
  // 其他违规内容
  '赌博', '毒品', '诈骗', '违法', '禁词', '敏感词'
];

// 敏感词检测函数
function checkSensitiveWords(text) {
  if (!text) return { hasSensitive: false, sensitiveWords: [] };
  
  const foundWords = [];
  const lowerText = text.toLowerCase();
  
  for (const word of SENSITIVE_WORDS) {
    if (text.includes(word) || lowerText.includes(word.toLowerCase())) {
      foundWords.push(word);
    }
  }
  
  // 变体检测
  const variantWords = {
    'sb': '傻逼', 'SB': '傻逼', '煞笔': '傻逼', '傻 B': '傻逼',
    '加微': '加微信', '薇信': '微信', 'V 信': '微信', '威信': '微信'
  };
  
  for (const variant of Object.keys(variantWords)) {
    if (text.includes(variant) || lowerText.includes(variant.toLowerCase())) {
      foundWords.push(variant);
    }
  }
  
  return {
    hasSensitive: foundWords.length > 0,
    sensitiveWords: [...new Set(foundWords)]
  };
}

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 数据库配置
const dbConfig = {
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '123456',
    database: process.env.DB_NAME || 'xundong_sanjiang',
    port: process.env.DB_PORT || 3306
};

// 创建全局连接池
let pool;

async function initPool() {
    try {
        pool = mysql.createPool(dbConfig);
        // 测试连接
        const [results] = await pool.execute('SELECT 1 + 1 AS result');
        console.log('Database connection test successful:', results);
        return pool;
    } catch (error) {
        console.error('Database pool initialization error:', error);
        throw error;
    }
}

// 通用数据库查询函数
async function query(sql) {
    if (!pool) {
        await initPool();
    }
    try {
        console.log('Executing query:', sql);
        const [results] = await pool.execute(sql);
        console.log('Query results:', results);
        return results;
    } catch (error) {
        console.error('Database query error:', error);
        console.error('Error stack:', error.stack);
        throw error;
    }
}

// ===== 认证工具函数 =====
function generateToken(userId, openid) {
    return Buffer.from(`${userId}:${openid}:${Date.now()}`).toString('base64');
}

function verifyToken(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ code: 401, message: '未登录' });
    }
    const token = authHeader.replace('Bearer ', '');
    try {
        const decoded = Buffer.from(token, 'base64').toString('utf8');
        const parts = decoded.split(':');
        if (parts.length < 2 || !parts[0] || !parts[1]) {
            return res.status(401).json({ code: 401, message: 'token无效' });
        }
        req.user = { id: parseInt(parts[0]), openid: parts[1] };
        next();
    } catch (e) {
        return res.status(401).json({ code: 401, message: 'token解析失败' });
    }
}

// 测试端点
app.get('/api/test', async (req, res) => {
    try {
        res.json({ code: 200, data: { message: 'Test API works!' } });
    } catch (error) {
        console.error('Test API error:', error);
        res.status(500).json({ code: 500, message: 'Test API failed' });
    }
});

// 景点API
app.get('/api/attractions', async (req, res) => {
    try {
        console.log('Processing /api/attractions request');
        console.log('Query params:', req.query);
        const { page = 1, limit = 10, category = '' } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let sql = 'SELECT * FROM attractions';

        if (category) {
            sql += ` WHERE category = '${category}'`;
        }

        sql += ` LIMIT ${parseInt(limit)} OFFSET ${parseInt(offset)}`;

        console.log('About to execute query:', sql);
        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取景点列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取景点列表失败' });
    }
});

app.get('/api/attractions/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM attractions WHERE id = ${parseInt(id)}`;
        const results = await query(sql);

        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '景点不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取景点详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取景点详情失败' });
    }
});

// 评论API - 帖子评论 (旧接口保留兼容)
app.get('/api/comments', async (req, res) => {
    try {
        const { post_id, page = 1, limit = 10, status } = req.query;
        const offset = (page - 1) * limit;

        let sql = `
            SELECT * FROM comments
            WHERE post_id = ${post_id}
        `;
        if (status !== undefined) {
            sql += ` AND status = ${status}`;
        }
        sql += ` ORDER BY created_at DESC
            LIMIT ${limit} OFFSET ${offset}
        `;
        
        const results = await query(sql);
        res.json({ code: 200, data: { records: results, total: results.length } });
    } catch (error) {
        console.error('获取评论失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取评论失败' });
    }
});

app.post('/api/comments', async (req, res) => {
    try {
        const { post_id, user_id, nickname, avatar, content, images } = req.body;

        if (!post_id || !user_id || !content) {
            return res.status(400).json({ code: 400, message: '参数不完整' });
        }

        // 敏感词检测
        const checkResult = checkSensitiveWords(content);
        if (checkResult.hasSensitive) {
            return res.status(400).json({
                code: 400,
                message: `内容包含敏感词：${checkResult.sensitiveWords.join('、')}，请修改后重新提交`
            });
        }

        const imagesStr = images ? `'${JSON.stringify(images)}'` : 'NULL';
        const sql = `INSERT INTO comments (post_id, user_id, nickname, avatar, content, images, created_at, status) VALUES (${post_id}, ${user_id}, '${nickname}', '${avatar}', '${content}', ${imagesStr}, NOW(), 1)`;
        const [result] = await pool.execute(sql);
        
        res.json({ code: 200, data: { id: result.insertId }, message: '评论发布成功' });
    } catch (error) {
        console.error('发布评论失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '发布评论失败' });
    }
});

// 评论API - 新接口 (posts下的评论
app.get('/api/posts/:postId/comments', async (req, res) => {
    try {
        const { postId } = req.params;
        const { page = 1, size = 20, status } = req.query;
        const offset = (page - 1) * size;

        let sql = `
            SELECT * FROM comments
            WHERE post_id = ${postId}
        `;
        if (status !== undefined) {
            sql += ` AND status = ${status}`;
        }
        sql += ` ORDER BY created_at DESC
            LIMIT ${size} OFFSET ${offset}
        `;
        
        const results = await query(sql);
        res.json({ code: 200, data: { records: results, total: results.length } });
    } catch (error) {
        console.error('获取评论失败:', error);
        res.status(500).json({ code: 500, message: '获取评论失败' });
    }
});

app.post('/api/posts/:postId/comments', async (req, res) => {
    try {
        const { postId } = req.params;
        const { user_id, nickname, avatar, content, images } = req.body;

        if (!postId || !user_id || !content) {
            return res.status(400).json({ code: 400, message: '参数不完整' });
        }

        // 敏感词检测
        const checkResult = checkSensitiveWords(content);
        if (checkResult.hasSensitive) {
            return res.status(400).json({
                code: 400,
                message: `内容包含敏感词：${checkResult.sensitiveWords.join('、')}，请修改后重新提交`
            });
        }

        const imagesStr = images ? `'${JSON.stringify(images)}'` : 'NULL';
        const sql = `INSERT INTO comments (post_id, user_id, nickname, avatar, content, images, created_at, status) VALUES (${postId}, ${user_id}, '${nickname}', '${avatar}', '${content}', ${imagesStr}, NOW(), 1)`;
        const [result] = await pool.execute(sql);
        
        res.json({ code: 200, data: { id: result.insertId }, message: '评论发布成功' });
    } catch (error) {
        console.error('发布评论失败:', error);
        res.status(500).json({ code: 500, message: '发布评论失败' });
    }
});

// 地图API
app.get('/api/map/nearby', async (req, res) => {
    try {
        const { lat, lng, radius = 5000, type = 'attraction' } = req.query;
        
        const sql = `
            SELECT *, 
                (6371 * acos(cos(radians(${lat})) * cos(radians(lat)) * cos(radians(lng) - radians(${lng})) + sin(radians(${lat})) * sin(radians(lat)))) AS distance
            FROM attractions
            HAVING distance <= ${radius}
            ORDER BY distance ASC
            LIMIT 20
        `;
        
        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取附近景点失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取附近景点失败' });
    }
});

app.get('/api/map/geocode', async (req, res) => {
    try {
        const { address } = req.query;
        const AK = 'QJMBZ-E7RCZ-FCFXR-7FS2E-WRIN5-ETBOK';
        const url = `https://apis.map.qq.com/ws/geocoder/v1/?address=${encodeURIComponent(address)}&key=${AK}`;
        
        const response = await axios.get(url);
        res.json({ code: 200, data: response.data });
    } catch (error) {
        console.error('地理编码失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '地理编码失败' });
    }
});

// 非遗API
app.get('/api/heritage', async (req, res) => {
    try {
        const { page = 1, limit = 10, level = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM heritage';

        if (level) {
            sql += ` WHERE level = '${level}'`;
        }

        sql += ` LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取非遗列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取非遗列表失败' });
    }
});

// 用户注册API
app.post('/api/users/register', async (req, res) => {
    try {
        const { phone, password, nickname, avatar, openid } = req.body;
        
        if (!phone || !password) {
            return res.status(400).json({ code: 400, message: '手机号和密码不能为空' });
        }
        
        const checkSql = `SELECT id FROM users WHERE phone = '${phone}'`;
        const existingUsers = await query(checkSql);
        
        if (existingUsers.length > 0) {
            return res.status(400).json({ code: 400, message: '该手机号已注册' });
        }
        
        const insertSql = `INSERT INTO users (phone, nickname, avatar, openid, status) VALUES ('${phone}', '${nickname || '新用户'}', '${avatar || ''}', '${openid || ''}', 1)`;
        const result = await query(insertSql);
        
        res.json({ code: 200, message: '注册成功', data: { id: result.insertId, phone, nickname: nickname || '新用户' } });
    } catch (error) {
        console.error('用户注册失败:', error);
        res.status(500).json({ code: 500, message: '注册失败' });
    }
});

// 用户登录API
app.post('/api/users/login', async (req, res) => {
    try {
        const { phone, password, code } = req.body;
        
        if (!phone) {
            return res.status(400).json({ code: 400, message: '账号不能为空' });
        }
        
        const sql = `SELECT * FROM users WHERE phone = '${phone}' OR nickname = '${phone}'`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '用户不存在，请先注册' });
        }
        
        const user = results[0];
        if (user.status === 0) {
            return res.status(403).json({ code: 403, message: '账号已被禁用' });
        }
        
        // 如果提供了密码，验证密码
        if (password) {
            // 简单密码验证（实际项目中应该使用 bcrypt 等加密）
            if (user.password && user.password !== password) {
                return res.status(401).json({ code: 401, message: '密码错误' });
            }
        }
        
        res.json({ 
            code: 200, 
            message: '登录成功', 
            data: { 
                id: user.id, 
                phone: user.phone, 
                nickname: user.nickname, 
                avatar: user.avatar 
            } 
        });
    } catch (error) {
        console.error('用户登录失败:', error);
        res.status(500).json({ code: 500, message: '登录失败' });
    }
});

// 验证验证码 API（测试用）
app.post('/api/users/verify-code', async (req, res) => {
    try {
        const { phone, code } = req.body;
        
        if (code === '123456') {
            return res.json({ code: 200, message: '验证码正确' });
        }
        
        res.status(400).json({ code: 400, message: '验证码错误' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '验证失败' });
    }
});

// 微信登录 API
app.post('/api/auth/wechat/login', async (req, res) => {
    try {
        const { code, avatarUrl } = req.body;
        
        if (!code) {
            return res.status(400).json({
                code: 400,
                message: 'code 不能为空'
            });
        }
        
        // 微信配置
        const APPID = 'wxb536d3e51941c674';
        const APPSECRET = '438db56f2535de2bfa52e9a9a87d1502';
        
        // 调用微信接口获取 openid
        const wxUrl = `https://api.weixin.qq.com/sns/jscode2session?appid=${APPID}&secret=${APPSECRET}&js_code=${code}&grant_type=authorization_code`;
        
        const wxResponse = await axios.get(wxUrl);
        const wxData = wxResponse.data;
        
        console.log('微信返回:', wxData);
        
        if (wxData.errcode) {
            return res.status(400).json({
                code: 400,
                message: wxData.errmsg || '微信登录失败' 
            });
        }
        
        const { openid, session_key } = wxData;
        
        // 查询用户是否存在
        let sql = `SELECT * FROM users WHERE openid = '${openid}'`;
        let results = await query(sql);
        
        let user;
        if (results.length === 0) {
            // 创建新用户
            const nickname = '微信用户' + Math.floor(Math.random() * 10000);
            const insertSql = `INSERT INTO users (openid, nickname, avatar, create_time)
                              VALUES ('${openid}', '${nickname}', '${avatarUrl || ''}', NOW())`;
            const [result] = await pool.execute(insertSql);
            
            user = { 
                id: result.insertId, 
                openid: openid, 
                nickname: nickname, 
                avatar: avatarUrl || '' 
            };
        } else {
            user = results[0];
            // 更新头像
            if (avatarUrl && user.avatar !== avatarUrl) {
                const updateSql = `UPDATE users SET avatar = '${avatarUrl}' WHERE id = ${user.id}`;
                await query(updateSql);
                user.avatar = avatarUrl;
            }
        }
        
        res.json({
            code: 200,
            message: '登录成功',
            data: {
                id: user.id,
                openid: user.openid,
                nickname: user.nickname,
                avatar: user.avatar,
                token: generateToken(user.id, user.openid)
            }
        });
    } catch (error) {
        console.error('微信登录失败:', error);
        res.status(500).json({ 
            code: 500, 
            message: '微信登录失败，请使用账号密码登录' 
        });
    }
});

// uni 一键登录 API
app.post('/api/auth/univerify/login', async (req, res) => {
    try {
        const { access_token, appid } = req.body;
        
        if (!access_token) {
            return res.status(400).json({
                code: 400,
                message: 'access_token 不能为空' 
            });
        }
        
        // TODO: 实际项目中应该调用 uniCloud 云函数换取手机号
        // 这里返回模拟数据用于测试
        console.log('一键登录请求 - access_token:', access_token);
        console.log('appid:', appid);
        
        // 模拟手机号（实际应从运营商网关获取）
        const mockPhoneNumber = '138****8888';
        
        // 检查用户是否存在
        const sql = `SELECT * FROM users WHERE phone LIKE '138%'`;
        const results = await query(sql);
        
        let user;
        if (results.length === 0) {
            // 创建新用户
            const insertSql = `INSERT INTO users (phone, nickname, avatar, create_time)
                              VALUES ('${mockPhoneNumber}', '三江游客', '', NOW())`;
            const [result] = await pool.execute(insertSql);
            user = { id: result.insertId, phone: mockPhoneNumber, nickname: '三江游客', avatar: '' };
        } else {
            user = results[0];
        }
        
        res.json({
            code: 200,
            message: '一键登录成功',
            data: {
                id: user.id,
                phone: user.phone,
                nickname: user.nickname,
                avatar: user.avatar,
                token: generateToken(user.id, ''),
                access_token: access_token
            }
        });
    } catch (error) {
        console.error('一键登录失败:', error);
        res.status(500).json({ 
            code: 500, 
            message: '一键登录失败，请使用其他登录方式' 
        });
    }
});

// 用户API
app.get('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM users WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '用户不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取用户信息失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取用户信息失败' });
    }
});

// 美食API
app.get('/api/foods', async (req, res) => {
    try {
        const { page = 1, limit = 10, category = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM foods';

        if (category) {
            sql += ` WHERE category = '${category}'`;
        }

        sql += ` LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        // 返回格式与前端期望一致，使用 records 包装
        res.json({ code: 200, data: { records: results } });
    } catch (error) {
        console.error('获取美食列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取美食列表失败' });
    }
});

app.get('/api/foods/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM foods WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '美食不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取美食详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取美食详情失败' });
    }
});

// 路线API
app.get('/api/routes', async (req, res) => {
    try {
        const { page = 1, limit = 10, difficulty = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM routes';

        if (difficulty) {
            sql += ` WHERE difficulty = '${difficulty}'`;
        }

        sql += ` LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取路线列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取路线列表失败' });
    }
});

app.get('/api/routes/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM routes WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '路线不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取路线详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取路线详情失败' });
    }
});

// 可用头像池（替换 pravatar.cc）
const avatarPool = [
    'https://cdn.phototourl.com/member/2026-03-27-41f55c65-23ae-4fb8-940c-9413cda51f87.jpg',
    'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png',
    'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png',
    'https://cdn.phototourl.com/member/2026-03-26-336fda84-2522-4265-bbcc-b9b5c0fe60d4.jpg',
    'https://cdn.phototourl.com/member/2026-03-26-81c8d874-4458-4d6d-b428-7e4849d6d873.jpg'
];

// 帖子 API (旧接口保留兼容)
app.get('/api/posts', async (req, res) => {
    try {
        const { page = 1, limit = 10, user_id = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM posts';

        if (user_id) {
            sql += ` WHERE user_id = ${user_id}`;
        }

        sql += ` ORDER BY create_time DESC LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        // 替换头像为可用 CDN 图片
        const data = results.map((item, index) => ({
            ...item,
            avatar: avatarPool[index % avatarPool.length]
        }));
        res.json({ code: 200, data: data });
    } catch (error) {
        console.error('获取帖子列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取帖子列表失败' });
    }
});

app.get('/api/posts/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM posts WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '帖子不存在' });
        }
        // 替换头像为可用 CDN 图片
        const data = {
            ...results[0],
            avatar: avatarPool[parseInt(id) % avatarPool.length]
        };
        res.json({ code: 200, data: data });
    } catch (error) {
        console.error('获取帖子详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取帖子详情失败' });
    }
});

// 社区动态 API (moments，和 posts 接口一致)
app.get('/api/moments', async (req, res) => {
    try {
        const { page = 1, size = 10, status, keyword, user_id = '' } = req.query;
        const offset = (page - 1) * size;

        let sql = 'SELECT * FROM posts WHERE 1=1';

        if (status !== undefined) {
            sql += ` AND status = '${status}'`;
        }
        if (user_id) {
            sql += ` AND user_id = ${user_id}`;
        }
        if (keyword) {
            sql += ` AND (content LIKE '%${keyword}%' OR title LIKE '%${keyword}%')`;
        }

        sql += ` ORDER BY create_time DESC LIMIT ${size} OFFSET ${offset}`;

        const results = await query(sql);
        // 替换头像为可用 CDN 图片
        const data = results.map((item, index) => ({
            ...item,
            author: item.author || item.nickname || '用户',
            avatar: avatarPool[index % avatarPool.length],
            create_time: item.create_time || item.created_at
        }));
        res.json({ code: 200, data: { records: data, total: data.length } });
    } catch (error) {
        console.error('获取社区动态列表失败:', error);
        res.status(500).json({ code: 500, message: '获取社区动态列表失败' });
    }
});

app.get('/api/moments/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM posts WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '帖子不存在' });
        }
        // 替换头像为可用 CDN 图片
        const data = {
            ...results[0],
            author: results[0].author || results[0].nickname || '用户',
            avatar: avatarPool[parseInt(id) % avatarPool.length],
            create_time: results[0].create_time || results[0].created_at
        };
        res.json({ code: 200, data: data });
    } catch (error) {
        console.error('获取帖子详情失败:', error);
        res.status(500).json({ code: 500, message: '获取帖子详情失败' });
    }
});

app.get('/api/moments/my', async (req, res) => {
    try {
        const { userId, page = 1, size = 10 } = req.query;
        const offset = (page - 1) * size;

        let sql = `SELECT * FROM posts WHERE user_id = ${userId} ORDER BY create_time DESC LIMIT ${size} OFFSET ${offset}`;

        const results = await query(sql);
        const data = results.map((item, index) => ({
            ...item,
            author: item.author || item.nickname || '用户',
            avatar: avatarPool[index % avatarPool.length]
        }));
        res.json({ code: 200, data: { records: data, total: data.length } });
    } catch (error) {
        console.error('获取我的动态失败:', error);
        res.status(500).json({ code: 500, message: '获取我的动态失败' });
    }
});

app.post('/api/moments/:id/like', async (req, res) => {
    try {
        const { id } = req.params;
        await query(`UPDATE posts SET likes = likes + 1 WHERE id = ${id}`);
        res.json({ code: 200, message: '点赞成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '点赞失败' });
    }
});

app.delete('/api/moments/:id', async (req, res) => {
    try {
        const { id } = req.params;
        await query(`DELETE FROM posts WHERE id = ${id}`);
        res.json({ code: 200, message: '删除成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '删除失败' });
    }
});

// 酒店API
app.get('/api/hotels', async (req, res) => {
    try {
        const { page = 1, limit = 10, type = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM hotels';

        if (type) {
            sql += ` WHERE type = '${type}'`;
        }

        sql += ` LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取酒店列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取酒店列表失败' });
    }
});

app.get('/api/hotels/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM hotels WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '酒店不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取酒店详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取酒店详情失败' });
    }
});

// 门票API
app.get('/api/tickets', async (req, res) => {
    try {
        const { page = 1, limit = 10, type = '' } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM tickets';

        if (type) {
            sql += ` WHERE type = '${type}'`;
        }

        sql += ` LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取门票列表失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取门票列表失败' });
    }
});

app.get('/api/tickets/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const sql = `SELECT * FROM tickets WHERE id = ${id}`;
        const results = await query(sql);
        
        if (results.length === 0) {
            return res.status(404).json({ code: 404, message: '门票不存在' });
        }
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        console.error('获取门票详情失败:', error);
        console.error('Error stack:', error.stack);
        res.status(500).json({ code: 500, message: '获取门票详情失败' });
    }
});

// ===== 聊天会话管理 API =====

// 创建新会话
app.post('/api/chat/sessions', verifyToken, async (req, res) => {
    try {
        const { title } = req.body;
        const userId = req.user.id;

        const [result] = await pool.execute(
            'INSERT INTO chat_sessions (user_id, title, created_at) VALUES (?, ?, NOW())',
            [userId, title || '新对话']
        );

        res.json({
            code: 200,
            data: { id: result.insertId }
        });
    } catch (error) {
        console.error('创建会话失败:', error);
        res.status(500).json({ code: 500, message: '创建会话失败' });
    }
});

// AI 聊天 API - 保存对话记录到数据库
app.post('/api/ai/chat', verifyToken, async (req, res) => {
    try {
        const { message, history = [], session_id } = req.body;
        const userId = req.user.id;

        if (!message) {
            return res.status(400).json({ code: 400, message: '消息不能为空' });
        }

        // 1. 获取或创建会话
        let sessionId = session_id;
        if (!sessionId) {
            const title = message.substring(0, 20) + (message.length > 20 ? '...' : '');
            const [result] = await pool.execute(
                'INSERT INTO chat_sessions (user_id, title, created_at) VALUES (?, ?, NOW())',
                [userId, title]
            );
            sessionId = result.insertId;
        }

        // 2. 获取当前消息序号
        const [countResult] = await pool.execute(
            'SELECT COUNT(*) as cnt FROM chat_messages WHERE session_id = ?',
            [sessionId]
        );
        let msgOrder = countResult[0].cnt + 1;

        // 3. 保存用户消息
        await pool.execute(
            'INSERT INTO chat_messages (session_id, role, content, msg_order, created_at) VALUES (?, ?, ?, ?, NOW())',
            [sessionId, 'user', message, msgOrder]
        );
        msgOrder++;

        // === 3.5 MCP 工具判断（优先于 LLM）===
        const toolResult = await mcpManager.executeTool(message, { userId, sessionId });

        if (toolResult) {
            // 工具匹配成功，保存工具结果并直接返回
            const toolContent = toolResult.content || '';
            const toolType = toolResult.type || null;
            const toolData = toolResult.data || toolResult.toolData || null;
            const itinerary = toolResult.itinerary || null;

            await pool.execute(
                `INSERT INTO chat_messages (session_id, role, content, msg_order, tool_type, tool_result, itinerary, created_at)
                 VALUES (?, ?, ?, ?, ?, ?, ?, NOW())`,
                [sessionId, 'ai', toolContent, msgOrder,
                 toolType,
                 toolData ? JSON.stringify(toolData) : null,
                 itinerary ? JSON.stringify(itinerary) : null]
            );

            // 更新会话元数据
            await pool.execute(
                'UPDATE chat_sessions SET message_count = ?, last_message_at = NOW(), updated_at = NOW() WHERE id = ?',
                [msgOrder, sessionId]
            );

            return res.json({
                code: 200,
                data: {
                    reply: toolContent,
                    session_id: sessionId,
                    toolType: toolType,
                    toolData: toolData,
                    itinerary: itinerary,
                    source: 'mcp_tool'
                }
            });
        }

        // === 4 无工具匹配，走 LLM ===
        // 千问大模型 API 配置
        const QWEN_API_KEY = process.env.QWEN_API_KEY || '';
        const QWEN_BASE_URL = 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions';
        const QWEN_MODEL = 'qwen-plus';

        // 4. 构建消息历史，只保留最近 10 条
        const recentHistory = history.slice(-10);
        const messages = [
            {
                role: 'system',
                content: `你是侗乡 AI 游览管家，专注于广西三江侗族自治县旅游服务。

你的职责：
1. 介绍三江景点：程阳八寨、三江风雨桥、三江鼓楼、丹洲古镇、布央茶园等
2. 推荐旅游路线：一日游、二日游、三日游等
3. 解答侗族文化：侗族大歌、风雨桥建造技艺、鼓楼营造技艺、侗锦织造等非遗文化
4. 提供交通住宿信息
5. 推荐美食特产：侗族腌鱼、油茶、糯米饭等
6. 提供购票信息和价格

回答要求：
- 使用中文回复，语言自然友好
- 适当使用 emoji 增加亲和力
- 信息准确，符合三江实际情况
- 回答简洁明了，重点突出`
            },
            ...recentHistory.map(m => ({
                role: m.role === 'user' ? 'user' : (m.role === 'ai' ? 'assistant' : 'user'),
                content: m.content
            })),
            {
                role: 'user',
                content: message
            }
        ];

        console.log('正在调用千问大模型 AI API...');

        // 5. 调用千问大模型 API
        const response = await axios.post(QWEN_BASE_URL, {
            model: QWEN_MODEL,
            messages: messages,
            max_tokens: 1024,
            temperature: 0.7,
            stream: false
        }, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${QWEN_API_KEY}`
            },
            timeout: 30000
        });

        console.log('千问大模型 API 响应成功');
        const reply = response.data.choices[0].message.content;

        // 6. 保存 AI 回复
        await pool.execute(
            'INSERT INTO chat_messages (session_id, role, content, msg_order, created_at) VALUES (?, ?, ?, ?, NOW())',
            [sessionId, 'ai', reply, msgOrder]
        );

        // 7. 更新会话元数据
        await pool.execute(
            'UPDATE chat_sessions SET message_count = ?, last_message_at = NOW(), updated_at = NOW() WHERE id = ?',
            [msgOrder, sessionId]
        );

        // 8. 保存到 ai_chat 表
        try {
            const category = detectCategory(message);
            await pool.execute(
                `INSERT INTO ai_chat (session_id, user_id, user_name, question, answer, category, response_time, tokens, has_sensitive, sensitive_words, flagged, ip, create_time)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())`,
                [sessionId, userId, userName, message, reply, category, responseTime, 0, 0, '[]', 0, req.ip]
            );
        } catch (err) {
            console.error('保存到 ai_chat 表失败:', err.message);
        }

        res.json({
            code: 200,
            data: {
                reply: reply,
                session_id: sessionId,
                model: QWEN_MODEL,
                usage: response.data.usage,
                source: 'qwen'
            }
        });
    } catch (error) {
        console.error('AI 聊天请求失败:', error.message);

        res.status(500).json({
            code: 500,
            message: 'AI 服务暂时不可用，请稍后重试',
            error: error.message
        });
    }
});

// ===== 聊天记录管理 API =====

// 获取用户会话列表
app.get('/api/chat/sessions', verifyToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const { page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        const [results] = await pool.execute(
            'SELECT id, title, message_count, last_message_at, created_at FROM chat_sessions WHERE user_id = ? AND status = 1 ORDER BY last_message_at DESC LIMIT ? OFFSET ?',
            [userId, parseInt(limit), offset]
        );

        const [countResult] = await pool.execute(
            'SELECT COUNT(*) as total FROM chat_sessions WHERE user_id = ? AND status = 1',
            [userId]
        );

        res.json({
            code: 200,
            data: {
                list: results,
                total: countResult[0].total,
                page: parseInt(page),
                limit: parseInt(limit)
            }
        });
    } catch (error) {
        console.error('获取会话列表失败:', error);
        res.status(500).json({ code: 500, message: '获取会话列表失败' });
    }
});

// 获取会话消息列表
app.get('/api/chat/sessions/:sessionId/messages', verifyToken, async (req, res) => {
    try {
        const { sessionId } = req.params;
        const userId = req.user.id;

        // 验证会话归属
        const [sessions] = await pool.execute(
            'SELECT id FROM chat_sessions WHERE id = ? AND user_id = ? AND status = 1',
            [sessionId, userId]
        );

        if (sessions.length === 0) {
            return res.status(404).json({ code: 404, message: '会话不存在' });
        }

        const { page = 1, limit = 50 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        const [messages] = await pool.execute(
            `SELECT id, role, content, tool_type, tool_result, itinerary, extra_data, msg_order, created_at
             FROM chat_messages WHERE session_id = ? ORDER BY msg_order ASC LIMIT ? OFFSET ?`,
            [sessionId, parseInt(limit), offset]
        );

        const formattedMessages = messages.map(msg => ({
            id: msg.id,
            role: msg.role,
            content: msg.content || '',
            toolType: msg.tool_type || null,
            toolResult: msg.tool_result ? JSON.parse(msg.tool_result) : null,
            itinerary: msg.itinerary ? JSON.parse(msg.itinerary) : null,
            msgOrder: msg.msg_order,
            createdAt: msg.created_at
        }));

        res.json({ code: 200, data: formattedMessages });
    } catch (error) {
        console.error('获取消息失败:', error);
        res.status(500).json({ code: 500, message: '获取消息失败' });
    }
});

// 保存消息（用于前端 MCP 工具结果）
app.post('/api/chat/messages', verifyToken, async (req, res) => {
    try {
        const { session_id, role, content, tool_type, tool_result, itinerary } = req.body;
        const userId = req.user.id;

        // 验证会话归属
        const [sessions] = await pool.execute(
            'SELECT id FROM chat_sessions WHERE id = ? AND user_id = ? AND status = 1',
            [session_id, userId]
        );

        if (sessions.length === 0) {
            return res.status(404).json({ code: 404, message: '会话不存在' });
        }

        const [countResult] = await pool.execute(
            'SELECT COUNT(*) as cnt FROM chat_messages WHERE session_id = ?',
            [session_id]
        );
        const msgOrder = countResult[0].cnt + 1;

        await pool.execute(
            `INSERT INTO chat_messages (session_id, role, content, tool_type, tool_result, itinerary, msg_order, created_at)
             VALUES (?, ?, ?, ?, ?, ?, ?, NOW())`,
            [session_id, role, content || '', tool_type || null, tool_result || null, itinerary || null, msgOrder]
        );

        await pool.execute(
            'UPDATE chat_sessions SET message_count = message_count + 1, last_message_at = NOW() WHERE id = ?',
            [session_id]
        );

        res.json({ code: 200, message: '保存成功' });
    } catch (error) {
        console.error('保存消息失败:', error);
        res.status(500).json({ code: 500, message: '保存消息失败' });
    }
});

// 删除会话（软删除）
app.delete('/api/chat/sessions/:sessionId', verifyToken, async (req, res) => {
    try {
        const { sessionId } = req.params;
        const userId = req.user.id;

        const [sessions] = await pool.execute(
            'SELECT id FROM chat_sessions WHERE id = ? AND user_id = ? AND status = 1',
            [sessionId, userId]
        );

        if (sessions.length === 0) {
            return res.status(404).json({ code: 404, message: '会话不存在' });
        }

        await pool.execute(
            'UPDATE chat_sessions SET status = 0, updated_at = NOW() WHERE id = ?',
            [sessionId]
        );

        res.json({ code: 200, message: '删除会话成功' });
    } catch (error) {
        console.error('删除会话失败:', error);
        res.status(500).json({ code: 500, message: '删除会话失败' });
    }
});

// 订单 API（购票）
app.get('/api/orders', async (req, res) => {
    try {
        const { user_id, status, page = 1, limit = 10 } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM orders WHERE 1=1';
        if (user_id) sql += ` AND user_id = ${user_id}`;
        if (status) sql += ` AND status = '${status}'`;
        sql += ` ORDER BY id DESC LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取订单列表失败:', error);
        res.status(500).json({ code: 500, message: '获取订单列表失败' });
    }
});

app.get('/api/orders/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const results = await query(`SELECT * FROM orders WHERE id = ${id}`);
        if (results.length === 0) return res.status(404).json({ code: 404, message: '订单不存在' });
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        res.status(500).json({ code: 500, message: '获取订单失败' });
    }
});

app.post('/api/orders', async (req, res) => {
    try {
        const { user_id, ticket_id, ticket_name, quantity, price, total_amount, contact_name, contact_phone, visit_date, remark } = req.body;

        if (!user_id || !ticket_id || !quantity) {
            return res.status(400).json({ code: 400, message: '参数不完整' });
        }

        const order_no = 'ORD' + Date.now();
        const sql = `INSERT INTO orders (order_no, user_id, ticket_id, ticket_name, quantity, price, total_amount, status, contact_name, contact_phone, visit_date, remark, create_time)
                     VALUES ('${order_no}', ${user_id}, ${ticket_id}, '${ticket_name}', ${quantity}, ${price}, ${total_amount}, 'pending', '${contact_name || ''}', '${contact_phone || ''}', '${visit_date || ''}', '${remark || ''}', NOW())`;
        const [result] = await pool.execute(sql);

        res.json({ code: 200, message: '下单成功', data: { id: result.insertId, order_no } });
    } catch (error) {
        console.error('创建订单失败:', error);
        res.status(500).json({ code: 500, message: '创建订单失败' });
    }
});

app.post('/api/orders/pay', async (req, res) => {
    try {
        const { order_no } = req.body;
        const results = await query(`SELECT * FROM orders WHERE order_no = '${order_no}'`);
        if (results.length === 0) return res.status(404).json({ code: 404, message: '订单不存在' });
        if (results[0].status !== 'pending') return res.status(400).json({ code: 400, message: '订单状态不允许支付' });

        await query(`UPDATE orders SET status = 'paid', pay_time = NOW() WHERE order_no = '${order_no}'`);
        res.json({ code: 200, message: '支付成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '支付失败' });
    }
});

app.post('/api/orders/cancel', async (req, res) => {
    try {
        const { order_no } = req.body;
        const results = await query(`SELECT * FROM orders WHERE order_no = '${order_no}'`);
        if (results.length === 0) return res.status(404).json({ code: 404, message: '订单不存在' });
        if (!['pending', 'paid'].includes(results[0].status)) return res.status(400).json({ code: 400, message: '订单状态不允许取消' });

        await query(`UPDATE orders SET status = 'cancelled' WHERE order_no = '${order_no}'`);
        res.json({ code: 200, message: '取消成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '取消失败' });
    }
});

// 行程规划 API
app.get('/api/journeys', async (req, res) => {
    try {
        const { user_id, page = 1, limit = 10 } = req.query;
        const offset = (page - 1) * limit;

        let sql = 'SELECT * FROM journeys WHERE 1=1';
        if (user_id) sql += ` AND user_id = ${user_id}`;
        sql += ` ORDER BY create_time DESC LIMIT ${limit} OFFSET ${offset}`;

        const results = await query(sql);
        res.json({ code: 200, data: results });
    } catch (error) {
        console.error('获取行程列表失败:', error);
        res.status(500).json({ code: 500, message: '获取行程列表失败' });
    }
});

app.get('/api/journeys/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const results = await query(`SELECT * FROM journeys WHERE id = ${id}`);
        if (results.length === 0) return res.status(404).json({ code: 404, message: '行程不存在' });
        res.json({ code: 200, data: results[0] });
    } catch (error) {
        res.status(500).json({ code: 500, message: '获取行程失败' });
    }
});

app.post('/api/journeys', async (req, res) => {
    try {
        const { user_id, name, start_date, end_date, description, attractions } = req.body;

        if (!user_id || !name) {
            return res.status(400).json({ code: 400, message: '参数不完整' });
        }

        const attractionsStr = attractions ? JSON.stringify(attractions) : '[]';
        const sql = `INSERT INTO journeys (user_id, name, start_date, end_date, description, attractions, create_time)
                     VALUES (${user_id}, '${name}', '${start_date || ''}', '${end_date || ''}', '${description || ''}', '${attractionsStr}', NOW())`;
        const [result] = await pool.execute(sql);

        res.json({ code: 200, message: '行程创建成功', data: { id: result.insertId } });
    } catch (error) {
        console.error('创建行程失败:', error);
        res.status(500).json({ code: 500, message: '创建行程失败' });
    }
});

app.put('/api/journeys/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, start_date, end_date, description, attractions } = req.body;
        const attractionsStr = attractions ? JSON.stringify(attractions) : '[]';

        await query(`UPDATE journeys SET name='${name}', start_date='${start_date || ''}', end_date='${end_date || ''}', description='${description || ''}', attractions='${attractionsStr}' WHERE id=${id}`);
        res.json({ code: 200, message: '行程更新成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '更新行程失败' });
    }
});

app.delete('/api/journeys/:id', async (req, res) => {
    try {
        const { id } = req.params;
        await query(`DELETE FROM journeys WHERE id = ${id}`);
        res.json({ code: 200, message: '行程删除成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '删除行程失败' });
    }
});

// 用户信息更新 API
app.put('/api/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { nickname, avatar, gender, birthday, bio } = req.body;

        const fields = [];
        if (nickname !== undefined) fields.push(`nickname='${nickname}'`);
        if (avatar !== undefined) fields.push(`avatar='${avatar}'`);
        if (gender !== undefined) fields.push(`gender=${gender}`);
        if (birthday !== undefined) fields.push(`birthday='${birthday}'`);
        if (bio !== undefined) fields.push(`bio='${bio}'`);

        if (fields.length === 0) return res.status(400).json({ code: 400, message: '没有要更新的字段' });

        await query(`UPDATE users SET ${fields.join(', ')} WHERE id = ${id}`);
        res.json({ code: 200, message: '用户信息更新成功' });
    } catch (error) {
        console.error('更新用户信息失败:', error);
        res.status(500).json({ code: 500, message: '更新用户信息失败' });
    }
});

// 帖子发布 API
app.post('/api/posts', async (req, res) => {
    try {
        const { user_id, title, content, images } = req.body;

        if (!user_id || !content) {
            return res.status(400).json({ code: 400, message: '参数不完整' });
        }

        // 敏感词检测
        const checkResult = checkSensitiveWords(content);
        if (checkResult.hasSensitive) {
            return res.status(400).json({
                code: 400,
                message: `内容包含敏感词：${checkResult.sensitiveWords.join('、')}，请修改后重新提交`
            });
        }

        const imagesStr = images ? JSON.stringify(images) : '[]';
        const sql = `INSERT INTO posts (user_id, title, content, images, status, likes, comments, create_time)
                     VALUES (${user_id}, '${title || ''}', '${content}', '${imagesStr}', 'pending', 0, 0, NOW())`;
        const [result] = await pool.execute(sql);

        res.json({ code: 200, message: '帖子发布成功，等待审核', data: { id: result.insertId } });
    } catch (error) {
        console.error('发布帖子失败:', error);
        res.status(500).json({ code: 500, message: '发布帖子失败' });
    }
});

// 帖子点赞 API
app.post('/api/posts/:id/like', async (req, res) => {
    try {
        const { id } = req.params;
        await query(`UPDATE posts SET likes = likes + 1 WHERE id = ${id}`);
        res.json({ code: 200, message: '点赞成功' });
    } catch (error) {
        res.status(500).json({ code: 500, message: '点赞失败' });
    }
});

// 工具函数：消息分类
function detectCategory(message) {
    const msg = message.toLowerCase();

    if (msg.includes('路线') || msg.includes('行程') || msg.includes('怎么去') || msg.includes('几天') || msg.includes('安排')) {
        return '路线规划';
    }
    if (msg.includes('美食') || msg.includes('好吃') || msg.includes('餐厅') || msg.includes('吃')) {
        return '美食推荐';
    }
    if (msg.includes('景点') || msg.includes('好玩') || msg.includes('门票') || msg.includes('哪里')) {
        return '景点推荐';
    }
    if (msg.includes('文化') || msg.includes('历史') || msg.includes('侗族') || msg.includes('传统')) {
        return '文化咨询';
    }
    if (msg.includes('交通') || msg.includes('公交') || msg.includes('大巴') || msg.includes('开车')) {
        return '交通查询';
    }
    if (msg.includes('酒店') || msg.includes('住宿') || msg.includes('民宿')) {
        return '住宿推荐';
    }

    return '其他';
}

// 根路径
app.get('/', (req, res) => {
    res.send('Welcome to Sanjiang API Server');
});

// 启动服务器
async function startServer() {
    try {
        await initPool();
        app.listen(PORT, () => {
            console.log(`Server running at http://127.0.0.1:${PORT}`);
            console.log('MySQL Connected...');
        });
    } catch (error) {
        console.error('Server startup error:', error);
        process.exit(1);
    }
}

startServer();

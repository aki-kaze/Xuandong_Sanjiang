const axios = require('axios');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://127.0.0.1:3000';

async function fetchData() {
  try {
    console.log('正在从数据库获取数据...');

    const dataDir = path.join(__dirname, '..', 'data');
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }

    const attractionsRes = await axios.get(`${BASE_URL}/api/attractions`, { params: { page: 1, limit: 100 } });
    const attractions = attractionsRes.data.success ? attractionsRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'attractions.json'), JSON.stringify(attractions, null, 2));
    console.log(`✓ 景点数据已保存 (${attractions.length} 条)`);

    const heritageRes = await axios.get(`${BASE_URL}/api/heritage`, { params: { page: 1, limit: 100 } });
    const heritage = heritageRes.data.success ? heritageRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'heritage.json'), JSON.stringify(heritage, null, 2));
    console.log(`✓ 非遗数据已保存 (${heritage.length} 条)`);

    const foodsRes = await axios.get(`${BASE_URL}/api/foods`, { params: { page: 1, limit: 100 } });
    const foods = foodsRes.data.success ? foodsRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'foods.json'), JSON.stringify(foods, null, 2));
    console.log(`✓ 美食数据已保存 (${foods.length} 条)`);

    const routesRes = await axios.get(`${BASE_URL}/api/routes`, { params: { page: 1, limit: 100 } });
    const routes = routesRes.data.success ? routesRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'routes.json'), JSON.stringify(routes, null, 2));
    console.log(`✓ 路线数据已保存 (${routes.length} 条)`);

    const hotelsRes = await axios.get(`${BASE_URL}/api/hotels`, { params: { page: 1, limit: 100 } });
    const hotels = hotelsRes.data.success ? hotelsRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'hotels.json'), JSON.stringify(hotels, null, 2));
    console.log(`✓ 酒店数据已保存 (${hotels.length} 条)`);

    const ticketsRes = await axios.get(`${BASE_URL}/api/tickets`, { params: { page: 1, limit: 100 } });
    const tickets = ticketsRes.data.success ? ticketsRes.data.data : [];
    fs.writeFileSync(path.join(dataDir, 'tickets.json'), JSON.stringify(tickets, null, 2));
    console.log(`✓ 门票数据已保存 (${tickets.length} 条)`);

    console.log('\n✅ 所有数据已成功保存到 data/ 目录！');

  } catch (error) {
    console.error('❌ 获取数据失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
    }
  }
}

fetchData();

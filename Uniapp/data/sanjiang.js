// data/sanjiang.js
// 统一数据入口 - 从 JSON 文件导入并导出

const attractions = require('./attractions.json')
const heritage = require('./heritage.json')
const foods = require('./foods.json')
const routes = require('./routes.json')
const hotels = require('./hotels.json')
const tickets = require('./tickets.json')

module.exports = {
  attractions,
  heritage,
  foods,
  routes,
  hotels,
  tickets
}

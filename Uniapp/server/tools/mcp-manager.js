/**
 * MCP工具管理器 - 后端版
 * 将前端的MCP工具逻辑迁移到后端
 */

const path = require('path');

// 加载真实静态数据
const attractions = require(path.join(__dirname, '../../data/attractions.json'));
const foods = require(path.join(__dirname, '../../data/foods.json'));
const routes = require(path.join(__dirname, '../../data/routes.json'));

class MCPToolManager {
  constructor() {
    this.tools = new Map();
    this.initTools();
  }

  // 初始化所有工具
  initTools() {
    this.tools.set('attraction', this.attractionTool.bind(this));
    this.tools.set('food', this.foodTool.bind(this));
    this.tools.set('itinerary', this.itineraryTool.bind(this));
    this.tools.set('weather', this.weatherTool.bind(this));
    this.tools.set('heritage', this.heritageTool.bind(this));
    this.tools.set('ticket', this.ticketTool.bind(this));
    this.tools.set('traffic', this.trafficTool.bind(this));
    this.tools.set('hotel', this.hotelTool.bind(this));
  }

  // 智能工具判断和执行
  async executeTool(text, context = {}) {
    const t = text.toLowerCase();

    // 景点查询
    if (this.isAttractionQuery(t)) {
      return await this.tools.get('attraction')({ text: t, context });
    }

    // 美食查询
    if (this.isFoodQuery(t)) {
      return await this.tools.get('food')({ text: t, context });
    }

    // 行程规划
    if (this.isItineraryQuery(t)) {
      return await this.tools.get('itinerary')({ text: t, context });
    }

    // 天气查询
    if (this.isWeatherQuery(t)) {
      return await this.tools.get('weather')({ text: t, context });
    }

    // 非遗文化
    if (this.isHeritageQuery(t)) {
      return await this.tools.get('heritage')({ text: t, context });
    }

    // 购票信息
    if (this.isTicketQuery(t)) {
      return await this.tools.get('ticket')({ text: t, context });
    }

    // 交通指南
    if (this.isTrafficQuery(t)) {
      return await this.tools.get('traffic')({ text: t, context });
    }

    // 酒店住宿
    if (this.isHotelQuery(t)) {
      return await this.tools.get('hotel')({ text: t, context });
    }

    return null; // 不匹配任何工具
  }

  // ========== 查询判断方法 ==========

  isAttractionQuery(text) {
    const keywords = ['三江景点', '景点推荐', '必去景点', '有哪些景点', '游玩', '景区', '古镇推荐', '茶园', '鼓楼', '风雨桥', '程阳八寨', '布央', '丹洲', '月也侗寨'];
    return keywords.some(k => text.includes(k));
  }

  isFoodQuery(text) {
    const keywords = ['三江美食', '三江吃什么', '侗族美食', '三江餐馆', '油茶', '酸鱼', '酸肉', '侗族糯米饭'];
    return keywords.some(k => text.includes(k));
  }

  isItineraryQuery(text) {
    const kw = ['三江一日游', '三江两日游', '三江三日游', '三江行程', '三江怎么玩', '三江游玩路线'];
    return kw.some(k => text.includes(k));
  }

  isWeatherQuery(text) {
    const kw = ['三江天气', '三江温度', '三江下雨', '三江气候'];
    return kw.some(k => text.includes(k));
  }

  isHeritageQuery(text) {
    const kw = ['三江非遗', '侗族非遗', '侗族大歌', '三江风雨桥', '三江鼓楼'];
    return kw.some(k => text.includes(k));
  }

  isTicketQuery(text) {
    const kw = ['三江门票', '景区购票', '三江买票', '三江票价'];
    return kw.some(k => text.includes(k));
  }

  isTrafficQuery(text) {
    const kw = ['去三江', '三江交通', '怎么去三江', '三江导航', '到三江'];
    return kw.some(k => text.includes(k));
  }

  isHotelQuery(text) {
    const kw = ['三江住宿', '三江酒店', '住哪里', '民宿', '客栈', '住什么', '住宿推荐'];
    return kw.some(k => text.includes(k));
  }

  // ========== 工具实现 ==========

  // 景点查询工具
  async attractionTool({ text, context }) {
    const list = this.filterAttractions(text);
    return {
      type: 'attraction',
      content: '为您推荐三江必去景点👇',
      data: list,
      toolResult: true,
      toolType: 'attraction',
      toolData: list
    };
  }

  // 美食查询工具
  async foodTool({ text, context }) {
    const list = this.filterFoods(text);
    return {
      type: 'food',
      content: '为您推荐三江特色美食👇',
      data: list,
      toolResult: true,
      toolType: 'food',
      toolData: list
    };
  }

  // 行程规划工具
  async itineraryTool({ text, context }) {
    const itinerary = this.generateItinerary(text);
    return {
      type: 'itinerary',
      content: '已为您规划推荐路线👇',
      itinerary: itinerary,
      role: 'ai'
    };
  }

  // 天气查询工具
  async weatherTool({ text, context }) {
    return {
      type: 'weather',
      content: '三江今日天气👇',
      data: {
        temp: '22',
        weather: '多云',
        humidity: '72',
        wind: '东北风2级'
      },
      toolResult: true,
      toolType: 'weather',
      toolData: {
        temp: '22',
        weather: '多云',
        humidity: '72',
        wind: '东北风2级'
      }
    };
  }

  // 非遗文化工具
  async heritageTool({ text, context }) {
    return {
      type: 'heritage',
      content: '为您介绍三江非遗文化，点击下方按钮前往非遗页面了解更多👇',
      toolResult: true,
      toolType: 'heritage',
      loading: false
    };
  }

  // 购票信息工具
  async ticketTool({ text, context }) {
    return {
      type: 'ticket',
      content: '为您提供三江景点购票信息，点击下方按钮前往购票页面👇',
      toolResult: true,
      toolType: 'ticket',
      loading: false
    };
  }

  // 交通指南工具
  async trafficTool({ text, context }) {
    return {
      type: 'traffic',
      content: '为您提供三江交通指南：\n\n🚗 自驾：从柳州出发约2小时车程\n🚌 公交：县城内有公交线路连接各景点\n🚕 打车：起步价5元，各景点间约10-30元\n\n点击下方按钮前往地图页面查看详细路线👇',
      toolResult: true,
      toolType: 'traffic',
      loading: false
    };
  }

  // 酒店住宿工具
  async hotelTool({ text, context }) {
    return {
      type: 'hotel',
      content: '为您推荐三江住宿推荐👇\n\n🏨 **三江县城**：交通便利，酒店齐全，价格100-300元/晚\n🏡 **程阳八寨**：推荐侗族特色民宿，体验当地文化，80-200元/晚\n🌿 **布央茶园周边**：生态民宿，空气清新，可体验采茶\n\n点击下方按钮跳转查看更多住宿详情👇',
      toolResult: true,
      toolType: 'hotel',
      loading: false
    };
  }

  // ========== 数据筛选方法 ==========

  filterAttractions(text) {
    let list = attractions.map(a => ({
      id: a.id,
      name: a.name,
      rating: a.rating,
      price: a.price,
      description: a.description,
      cover: a.cover,
      category: a.category
    }));

    if (text.includes('必去') || text.includes('推荐') || text.includes('好玩')) {
      return list.slice(0, 5);
    }
    if (text.includes('古镇') || text.includes('程阳')) {
      return list.filter(a => a.name.includes('程阳') || a.category === '古村落');
    }
    if (text.includes('茶园') || text.includes('布央')) {
      return list.filter(a => a.name.includes('布央') || a.category === '自然风光');
    }
    if (text.includes('自然') || text.includes('山水')) {
      return list.filter(a => a.category === '自然风光');
    }

    return list.slice(0, 5);
  }

  filterFoods(text) {
    let list = foods.map(f => ({
      id: f.id,
      name: f.name,
      rating: parseFloat(f.rating) || 4.5,
      price: f.price,
      description: f.description,
      cover: f.cover
    }));

    if (text.includes('酸鱼') || text.includes('酸肉')) {
      return list.filter(f => f.name.includes('酸') || f.name.includes('鱼') || f.name.includes('肉'));
    }
    if (text.includes('油茶')) {
      return list.filter(f => f.name.includes('油茶'));
    }
    if (text.includes('糯米饭') || text.includes('糯')) {
      return list.filter(f => f.name.includes('糯'));
    }
    if (text.includes('推荐') || text.includes('特色')) {
      return list;
    }

    return list;
  }

  generateItinerary(text) {
    const day1 = [
      { time: '09:00', title: '程阳八寨', desc: '观赏世界最长风雨桥，体验侗族鼓楼文化', traffic: '县城打车25分钟' },
      { time: '12:00', title: '侗族午餐', desc: '品尝酸鱼、酸肉、油茶', traffic: '' },
      { time: '14:00', title: '侗族大歌表演', desc: '世界级非物质文化遗产', traffic: '' },
      { time: '17:00', title: '返回县城', desc: '结束行程', traffic: '' }
    ];

    const day2 = [
      { time: '09:30', title: '布央仙人山', desc: '万亩茶园，云海观景', traffic: '公交50分钟' },
      { time: '12:30', title: '茶园餐厅', desc: '茶餐特色宴', traffic: '' },
      { time: '14:30', title: '丹洲古镇', desc: '明代古县城，四面环水', traffic: '自驾40分钟' }
    ];

    const day3 = [
      { time: '09:00', title: '月也侗寨', desc: '侗族文化体验村', traffic: '县城打车15分钟' },
      { time: '12:00', title: '特色午餐', desc: '侗族风味美食', traffic: '' },
      { time: '14:00', title: '非遗体验馆', desc: '体验侗锦织造、蜡染', traffic: '' },
      { time: '16:00', title: '自由活动', desc: '购买特产，准备返程', traffic: '' }
    ];

    const day4 = [
      { time: '08:30', title: '深度体验程阳八寨', desc: '走进村寨，探访侗族人家', traffic: '' },
      { time: '11:30', title: '农家午餐', desc: '品尝地道农家菜', traffic: '' },
      { time: '13:30', title: '侗族手工艺体验', desc: '学习制作银饰、侗锦', traffic: '' },
      { time: '16:00', title: '返程准备', desc: '整理行李，结束愉快的三江之旅', traffic: '' }
    ];

    if (text.includes('一日')) {
      return {
        title: '三江精品一日游',
        days: [{ day: 1, date: '当日', plans: day1 }]
      };
    }

    if (text.includes('两日')) {
      return {
        title: '三江深度两日游',
        days: [
          { day: 1, date: '第一天', plans: day1 },
          { day: 2, date: '第二天', plans: day2 }
        ]
      };
    }

    if (text.includes('三日')) {
      return {
        title: '三江完整三日游',
        days: [
          { day: 1, date: '第一天', plans: day1 },
          { day: 2, date: '第二天', plans: day2 },
          { day: 3, date: '第三天', plans: day3 }
        ]
      };
    }

    if (text.includes('四日')) {
      return {
        title: '三江深度四日游',
        days: [
          { day: 1, date: '第一天', plans: day1 },
          { day: 2, date: '第二天', plans: day2 },
          { day: 3, date: '第三天', plans: day3 },
          { day: 4, date: '第四天', plans: day4 }
        ]
      };
    }

    // 默认返回路线推荐
    return {
      title: '三江推荐路线',
      days: [{ day: 1, date: '推荐', plans: day1 }]
    };
  }
}

module.exports = MCPToolManager;

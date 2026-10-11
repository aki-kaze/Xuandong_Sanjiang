import attractionsData from '@/data/attractions.json'
import heritageData from '@/data/heritage.json'
import foodsData from '@/data/foods.json'
import routesData from '@/data/routes.json'
import hotelsData from '@/data/hotels.json'
import ticketsData from '@/data/tickets.json'

function parseJsonField(obj, field) {
  if (obj && typeof obj[field] === 'string') {
    try {
      obj[field] = JSON.parse(obj[field])
    } catch (e) {
      console.warn(`解析 ${field} 失败:`, e)
      if (!obj[field]) obj[field] = []
    }
  }
  return obj
}

function processData(data) {
  return data.map(item => {
    parseJsonField(item, 'tags')
    parseJsonField(item, 'timeline')
    parseJsonField(item, 'relatedTicketIds')
    parseJsonField(item, 'stops')
    parseJsonField(item, 'includes')
    parseJsonField(item, 'notice')
    return item
  })
}

export const staticData = {
  attractions: processData(attractionsData),
  heritage: processData(heritageData),
  foods: processData(foodsData),
  routes: processData(routesData),
  hotels: processData(hotelsData),
  tickets: processData(ticketsData)
}

export function getAttractionById(id) {
  return staticData.attractions.find(a => a.id === parseInt(id))
}

export function getHeritageById(id) {
  return staticData.heritage.find(h => h.id === parseInt(id))
}

export function getFoodById(id) {
  return staticData.foods.find(f => f.id === parseInt(id))
}

export function getRouteById(id) {
  return staticData.routes.find(r => r.id === parseInt(id))
}

export function getHotelById(id) {
  return staticData.hotels.find(h => h.id === parseInt(id))
}

export function getTicketById(id) {
  return staticData.tickets.find(t => t.id === parseInt(id))
}

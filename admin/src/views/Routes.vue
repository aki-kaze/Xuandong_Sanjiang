<template>
  <div class="routes-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">路线与自定义规划</h2>
        <p class="page-sub">管理推荐路线和用户自定义路线规划</p>
      </div>
      <div class="header-right">
        <div class="button-row">
          <el-button v-if="activeTab === 'routes'" type="primary" plain class="btn-glow" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加路线
          </el-button>
        </div>
      </div>
    </div>

    <!-- Tab -->
    <div class="tab-bar glass-card">
      <div class="tab-group">
        <div class="tab-item" :class="{ active: activeTab === 'routes' }" @click="activeTab = 'routes'; loadData()">
          <el-icon><Guide /></el-icon>
          路线管理
          <span class="tab-count">{{ routeList.length }}</span>
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'journeys' }" @click="activeTab = 'journeys'; loadData()">
          <el-icon><Timer /></el-icon>
          自定义路线规划
          <span class="tab-count">{{ journeyTotal }}</span>
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'map' }" @click="activeTab = 'map'; initMap()">
          <el-icon><MapLocation /></el-icon>
          旅游地图
        </div>
      </div>
    </div>

    <!-- 路线管理 -->
    <template v-if="activeTab === 'routes'">
      <div class="search-bar glass-card">
        <el-input v-model="routeSearch.keyword" placeholder="搜索路线名称..." clearable style="width:240px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="routeSearch.difficulty" placeholder="难度筛选" clearable style="width:130px;margin-left:10px">
          <el-option label="简单" value="简单" />
          <el-option label="中等" value="中等" />
          <el-option label="困难" value="困难" />
        </el-select>
        <div class="button-row" style="margin-left:8px">
          <el-button @click="loadData">搜索</el-button>
        </div>
      </div>

      <div class="table-card glass-card">
        <el-table :data="filteredRoutes" v-loading="loading" stripe>
          <el-table-column label="#" width="60" align="center">
            <template #default="{ $index }">
              <span style="font-weight: 600; color: #1D3B5A;">{{ $index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="封面" width="80">
            <template #default="{ row }">
              <el-image :src="row.cover" fit="cover" style="width:56px;height:42px;border-radius:6px" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="路线名称" min-width="150">
            <template #default="{ row }">
              <span style="font-weight:600;color:#1D3B5A">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.difficulty==='简单'?'success':row.difficulty==='中等'?'warning':'danger'" effect="dark">{{ row.difficulty }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="距离" width="90">
            <template #default="{ row }">{{ row.distance }}km</template>
          </el-table-column>
          <el-table-column label="站点数" width="70">
            <template #default="{ row }">{{ parseJsonField(row.stops, []).length }}</template>
          </el-table-column>
          <el-table-column label="价格" width="90">
            <template #default="{ row }">
              <span style="color:#A33B32;font-weight:700">¥{{ row.price || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标签" width="120">
            <template #default="{ row }">
              <el-tag v-for="tag in parseJsonField(row.tags, []).slice(0, 2)" :key="tag" size="small" type="info" style="margin-right:4px">{{ tag }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="dark">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <div class="button-row">
                  <el-button :type="row.status === 1 ? 'warning' : 'success'" plain size="small" @click="handleToggleRouteStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
                  <el-button type="primary" plain size="small" @click="handleEditRoute(row)">编辑</el-button>
                  <el-button type="danger" plain size="small" @click="handleDeleteRoute(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <!-- 自定义路线规划 -->
    <template v-if="activeTab === 'journeys'">
      <div class="search-bar glass-card">
        <el-input v-model="journeySearch.keyword" placeholder="搜索规划名称..." clearable style="width:220px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="journeySearch.userId" placeholder="用户ID" clearable style="width:120px;margin-left:10px" />
        <el-select v-model="journeySearch.status" placeholder="状态" clearable style="width:120px;margin-left:10px">
          <el-option label="已保存" value="1" />
          <el-option label="草稿" value="0" />
          <el-option label="未开始" value="未开始" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
        </el-select>
        <div class="button-row" style="margin-left:8px">
          <el-button @click="loadData">搜索</el-button>
        </div>
      </div>

      <div class="table-card glass-card">
        <el-table :data="journeyList" v-loading="loading" stripe>
          <el-table-column label="#" width="60" align="center">
            <template #default="{ $index }">
              <span style="font-weight: 600; color: #1D3B5A;">{{ (journeyPage - 1) * journeySize + $index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="封面" width="80">
            <template #default="{ row }">
              <el-image 
                :src="getJourneyCover(row)" 
                fit="cover" 
                style="width:56px;height:42px;border-radius:6px" 
                :fallback-src="'https://via.placeholder.com/56x42/4A90E2/ffffff?text=NO+IMG'"
              />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="规划名称" min-width="140">
            <template #default="{ row }">
              <span style="font-weight:600;color:#1D3B5A">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column label="天数/人数" width="100">
            <template #default="{ row }">{{ row.days || 0 }}天 / {{ row.people || 1 }}人</template>
          </el-table-column>
          <el-table-column label="景点数" width="70">
            <template #default="{ row }">
              {{ getJourneyStopCount(row) }}
            </template>
          </el-table-column>
          <el-table-column label="预算" width="100">
            <template #default="{ row }">
              <span style="color:#A33B32;font-weight:700">
                {{ row.budget ? (typeof row.budget === 'string' ? row.budget : '¥' + row.budget) : '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="journeyStatusType(row.status)" size="small" effect="dark">
                {{ typeof row.status === 'number' ? (row.status === 1 ? '已保存' : '草稿') : (row.status || '未开始') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="交通" width="80">
            <template #default="{ row }">{{ transportLabel(row.transport) || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">{{ row.createdAt || row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <div class="button-row">
                  <el-button type="primary" plain size="small" @click="handleEditJourney(row)">查看</el-button>
                  <el-button type="danger" plain size="small" @click="handleDeleteJourney(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="journeyPage"
            v-model:page-size="journeySize"
            :total="journeyTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </div>
    </template>

    <!-- 旅游地图 -->
    <template v-if="activeTab === 'map'">
      <div class="map-toolbar glass-card">
        <div class="map-legend">
          <span class="legend-title">图例：</span>
          <span class="legend-item"><span class="legend-dot" style="background:#4A90E2"></span> 景点 ({{ mapData.attractions.length }})</span>
          <span class="legend-item"><span class="legend-dot" style="background:#D4A373"></span> 非遗 ({{ mapData.heritages.length }})</span>
          <span class="legend-item"><span class="legend-dot" style="background:#A33B32"></span> 活动 ({{ mapData.activities.length }})</span>
        </div>
        <div class="map-filters">
          <el-checkbox v-model="mapFilters.attractions" @change="refreshMapMarkers">景点</el-checkbox>
          <el-checkbox v-model="mapFilters.heritages" @change="refreshMapMarkers">非遗</el-checkbox>
          <el-checkbox v-model="mapFilters.activities" @change="refreshMapMarkers">活动</el-checkbox>
        </div>
      </div>
      <div class="map-container glass-card" id="sanjiang-map"></div>
    </template>

    <!-- 路线编辑弹窗 -->
    <el-dialog
      v-model="routeDialogVisible"
      :title="isRouteEdit ? '编辑路线' : '添加路线'"
      width="900px"
      class="route-edit-dialog"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div v-if="routeForm" class="route-form-container">
        <!-- 基本信息 -->
        <div class="form-section">
          <h4 class="section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </h4>
          <el-form :model="routeForm" label-width="90px" :rules="routeRules" ref="routeFormRef">
            <el-form-item label="路线名称" prop="name">
              <el-input v-model="routeForm.name" placeholder="请输入路线名称，如：三江精华一日游" maxlength="50" show-word-limit />
            </el-form-item>
            
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="难度" prop="difficulty">
                  <el-select v-model="routeForm.difficulty" placeholder="请选择" style="width:100%">
                    <el-option label="简单" value="简单">
                      <span style="float: left">简单</span>
                      <span style="float: right; color: #67C23A; font-size: 13px">适合家庭</span>
                    </el-option>
                    <el-option label="中等" value="中等">
                      <span style="float: left">中等</span>
                      <span style="float: right; color: #E6A23C; font-size: 13px">需一定体力</span>
                    </el-option>
                    <el-option label="困难" value="困难">
                      <span style="float: left">困难</span>
                      <span style="float: right; color: #F56C6C; font-size: 13px">专业级别</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="距离(km)" prop="distance">
                  <el-input-number
                    v-model="routeForm.distance"
                    :min="0"
                    :max="9999"
                    :step="0.1"
                    :precision="1"
                    style="width:100%"
                    placeholder="请输入"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="旅游天数" prop="days">
                  <el-input-number
                    v-model="routeForm.days"
                    :min="1"
                    :max="30"
                    :step="1"
                    style="width:100%"
                    placeholder="请输入天数"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="价格(元)" prop="price">
                  <el-input-number
                    v-model="routeForm.price"
                    :min="0"
                    :max="999999"
                    :step="10"
                    style="width:100%"
                    placeholder="请输入"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="封面图片">
              <div class="cover-upload-section">
                <div class="cover-input-group">
                  <el-input
                    v-model="routeForm.cover"
                    placeholder="请输入图片 URL 或使用下方快捷选择"
                    clearable
                  >
                    <template #prepend>
                      <el-icon><Picture /></el-icon>
                    </template>
                  </el-input>
                  <el-button type="primary" @click="openAttractionCoverSelector">
                    <el-icon><Search /></el-icon>
                    从景点选择
                  </el-button>
                </div>

                <!-- 封面预览 -->
                <div class="cover-preview-area" v-if="routeForm.cover">
                  <el-image
                    :src="routeForm.cover"
                    fit="cover"
                    class="cover-preview-img"
                    :preview-src-list="[routeForm.cover]"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>加载失败</span>
                      </div>
                    </template>
                  </el-image>
                  <div class="cover-preview-actions">
                    <el-button size="small" type="primary" link @click="previewCover">
                      <el-icon><ZoomIn /></el-icon>
                      预览
                    </el-button>
                    <el-button size="small" type="danger" link @click="routeForm.cover = ''">
                      <el-icon><Delete /></el-icon>
                      移除
                    </el-button>
                  </div>
                </div>
                <div class="cover-placeholder" v-else>
                  <el-icon><Picture /></el-icon>
                  <span>暂无封面</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="路线状态">
              <el-switch
                v-model="routeForm.status"
                :active-value="1"
                :inactive-value="0"
                active-text="上架"
                inactive-text="下架"
                inline-prompt
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #909399"
              />
              <span class="status-hint">{{ routeForm.status === 1 ? '用户可见' : '用户不可见' }}</span>
            </el-form-item>
            
            <!-- 标签管理 -->
            <el-form-item label="路线标签">
              <div class="tags-manager">
                <div class="selected-tags">
                  <el-tag 
                    v-for="(tag, idx) in routeTags" 
                    :key="idx" 
                    closable 
                    size="small" 
                    type="primary"
                    class="selected-tag"
                    @close="removeRouteTag(idx)"
                  >
                    {{ tag }}
                  </el-tag>
                  <el-input 
                    v-model="newRouteTag" 
                    placeholder="输入标签后回车" 
                    class="tag-input"
                    size="small"
                    @keyup.enter="addRouteTag" 
                    @blur="addRouteTag"
                  />
                </div>
                <div class="quick-tags">
                  <span class="quick-tags-label">快捷添加：</span>
                  <el-tag 
                    size="small" 
                    :type="routeTags.includes(qt) ? 'primary' : ''"
                    class="quick-tag"
                    v-for="qt in quickRouteTags" 
                    :key="qt" 
                    @click="quickAddRouteTag(qt)"
                  >
                    {{ qt }}
                  </el-tag>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 行程安排 -->
        <div class="form-section">
          <div class="section-header">
            <h4 class="section-title">
              <el-icon><List /></el-icon>
              行程安排
              <el-tag size="small" type="info" class="stop-count-tag">{{ routeStops.length }} 个站点</el-tag>
            </h4>
            <div class="section-actions">
              <el-button type="primary" size="small" @click="openAttractionSelector">
                <el-icon><Plus /></el-icon>
                添加景点
              </el-button>
              <el-button type="warning" size="small" @click="openHotelSelector">
                <el-icon><Plus /></el-icon>
                添加酒店
              </el-button>
            </div>
          </div>
          
          <!-- 行程统计 -->
          <el-card shadow="never" class="route-stats-card" v-if="routeStops.length > 0">
            <el-descriptions :column="4" size="small" border>
              <el-descriptions-item label="总站点数">
                <el-tag type="primary">{{ routeStops.length }} 个</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="门票合计">
                <el-tag type="warning">¥{{ totalTicketPrice }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="酒店费用">
                <el-tag type="success">¥{{ totalHotelPrice }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="交通费用">
                <el-tag type="info">¥{{ totalTransportCost }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <el-divider style="margin: 12px 0" />
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div style="font-size: 14px; color: #606266;">
                自动计算价格 = 门票合计 + 酒店费用 + 交通费用
              </div>
              <el-button type="primary" size="small" @click="autoCalcPrice" :disabled="routeStops.length === 0">
                <el-icon><MagicStick /></el-icon>
                自动计算价格
              </el-button>
            </div>
          </el-card>
          
          <!-- 站点列表 -->
          <div class="stops-list" v-if="routeStops.length > 0">
            <div 
              v-for="(stop, idx) in routeStops" 
              :key="idx" 
              class="stop-card"
              :class="{ 'dragging': draggingIndex === idx }"
              draggable="true"
              @dragstart="onDragStart(idx)"
              @dragend="onDragEnd"
              @dragover.prevent="onDragOver(idx)"
              @drop="onDrop(idx)"
            >
              <div class="stop-card-header">
                <div class="stop-number">
                  <span class="stop-num-badge">{{ idx + 1 }}</span>
                </div>
                <div class="stop-info">
                  <div class="stop-name">{{ stop.name || '未命名景点' }}</div>
                  <div class="stop-details">
                    <span class="stop-detail-item" v-if="stop.ticketPrice !== undefined && stop.ticketPrice !== null && stop.type !== 'hotel'">
                      <el-icon><Coin /></el-icon>
                      {{ stop.ticketPrice > 0 ? `¥${stop.ticketPrice}` : '免费' }}
                    </span>
                    <span class="stop-detail-item" v-if="stop.hotelPrice !== undefined && stop.hotelPrice !== null && stop.type === 'hotel'">
                      <el-icon><Coin /></el-icon>
                      {{ stop.hotelPrice > 0 ? `¥${stop.hotelPrice}` : '免费' }}
                    </span>
                  </div>
                </div>
                <div class="stop-transport-selector">
                  <el-select v-model="stop.transport" placeholder="选择交通方式" size="small" style="width: 120px" @change="onTransportChange(stop)">
                    <el-option label="步行" value="walking">
                      <span>步行</span>
                      <span style="float: right; color: #67C23A; font-size: 12px">¥0</span>
                    </el-option>
                    <el-option label="公交" value="bus">
                      <span>公交</span>
                      <span style="float: right; color: #E6A23C; font-size: 12px">¥2</span>
                    </el-option>
                    <el-option label="自驾" value="driving">
                      <span>自驾</span>
                      <span style="float: right; color: #409EFF; font-size: 12px">¥10</span>
                    </el-option>
                  </el-select>
                </div>
                <div class="stop-actions">
                  <el-tooltip content="上移" placement="top">
                    <el-button 
                      size="small" 
                      type="primary" 
                      link 
                      :disabled="idx === 0" 
                      @click="moveRouteStopUp(idx)"
                    >
                      <el-icon><Top /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="下移" placement="top">
                    <el-button 
                      size="small" 
                      type="primary" 
                      link 
                      :disabled="idx === routeStops.length - 1" 
                      @click="moveRouteStopDown(idx)"
                    >
                      <el-icon><Bottom /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="编辑" placement="top">
                    <el-button size="small" type="primary" link @click="editRouteStop(idx)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button size="small" type="danger" link @click="removeRouteStop(idx)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="拖拽排序" placement="top">
                    <el-button size="small" link class="drag-handle">
                      <el-icon><Rank /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
              <div class="stop-desc" v-if="stop.desc">
                {{ stop.desc }}
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <el-empty v-else description="暂无行程安排" :image-size="80">
            <template #description>
              <p>点击"添加景点"从景点库中选择景点来规划行程</p>
            </template>
            <div class="empty-actions">
              <el-button type="primary" @click="openAttractionSelector">
                <el-icon><Plus /></el-icon>
                添加景点
              </el-button>
            </div>
          </el-empty>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="routeDialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleSaveRoute" :loading="savingRoute" size="large" class="save-btn">
            <el-icon v-if="!savingRoute"><Check /></el-icon>
            {{ savingRoute ? '保存中...' : (isRouteEdit ? '保存修改' : '创建路线') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 站点编辑弹窗 -->
    <el-dialog
      v-model="stopDialogVisible"
      :title="editingStopIdx >= 0 ? '编辑站点' : '添加站点'"
      width="600px"
      class="stop-edit-dialog"
      top="10vh"
      :close-on-click-modal="false"
    >
      <el-form :model="editingStop" label-width="100px" :rules="stopRules" ref="stopFormRef" class="stop-form">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editingStop.name" placeholder="请从景点或酒店库选择" readonly>
            <template #append>
              <el-button @click="openAttractionSelectorForEdit" style="margin-right: 4px">
                <el-icon><Search /></el-icon>
                选择景点
              </el-button>
              <el-button @click="openHotelSelectorForEdit" type="warning">
                <el-icon><Search /></el-icon>
                选择酒店
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="交通方式">
          <el-radio-group v-model="editingStop.transport" class="transport-group">
            <el-radio-button value="walking">🚶 步行</el-radio-button>
            <el-radio-button value="bus">🚌 公交</el-radio-button>
            <el-radio-button value="driving">🚗 自驾</el-radio-button>
            <el-radio-button value="cablecar">🚡 缆车</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="交通费用">
          <el-input-number
            v-model="editingStop.transportCost"
            :min="0"
            :max="9999"
            :step="5"
            style="width:100%"
            controls-position="right"
            placeholder="请输入交通费用"
          >
            <template #prefix>¥</template>
          </el-input-number>
          <div style="font-size:12px;color:#909399;margin-top:6px">
            <el-icon style="vertical-align: middle;"><InfoFilled /></el-icon>
            从上一站点到此站点的交通费用，步行可填0
          </div>
        </el-form-item>

        <el-form-item label="门票价格">
          <el-input-number
            v-model="editingStop.ticketPrice"
            :min="0"
            :max="9999"
            :step="5"
            style="width:100%"
            controls-position="right"
            placeholder="请输入门票价格"
          >
            <template #prefix>¥</template>
          </el-input-number>
          <div style="font-size:12px;color:#909399;margin-top:6px">
            <el-icon style="vertical-align: middle;"><InfoFilled /></el-icon>
            输入0表示免费景点，从景点库选择时会自动填充门票价格
          </div>
        </el-form-item>

        <el-form-item label="景点描述">
          <el-input
            v-model="editingStop.desc"
            type="textarea"
            :rows="3"
            placeholder="如: 参观中国最长风雨桥，网红打卡地点"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stopDialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="saveRouteStop" size="large">
            <el-icon><Check /></el-icon>
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 景点封面选择器弹窗 -->
    <el-dialog v-model="coverSelectorVisible" title="选择封面图片" width="700px" top="8vh">
      <el-input v-model="attractionSearch" placeholder="搜索景点名称..." prefix-icon="Search" style="margin-bottom:16px" clearable />
      <div class="cover-selector-grid">
        <div
          v-for="attraction in filteredAttractions.slice(0, 20)"
          :key="attraction.id"
          class="cover-item"
          @click="selectAttractionCover(attraction)"
          :class="{ 'no-cover': !attraction.cover && !attraction.image }"
        >
          <el-image
            :src="attraction.cover || attraction.image"
            fit="cover"
            class="cover-item-img"
          >
            <template #error>
              <div class="cover-item-error">
                <el-icon><Picture /></el-icon>
                <span>无图片</span>
              </div>
            </template>
          </el-image>
          <div class="cover-item-name">{{ attraction.name }}</div>
        </div>
      </div>
      <el-empty v-if="filteredAttractions.length === 0" description="暂无匹配的景点" :image-size="80" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="coverSelectorVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 景点选择器弹窗 -->
    <el-dialog v-model="attractionSelectorVisible" title="从景点库选择" width="720px" top="8vh">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #default>
          <div style="font-size:13px">
            <el-icon style="vertical-align: middle;"><InfoFilled /></el-icon>
            选择景点后将自动填充景点名称、描述和门票价格
          </div>
        </template>
      </el-alert>
      <el-input v-model="attractionSearch" placeholder="搜索景点名称..." prefix-icon="Search" style="margin-bottom:16px" clearable />
      <el-table :data="filteredAttractions" max-height="400" highlight-current-row @current-change="onAttractionSelect" @row-dblclick="onAttractionDblClick" empty-text="暂无匹配的景点">
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image :src="row.cover || row.image" fit="cover" style="width:60px;height:45px;border-radius:4px" lazy />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="景点名称" min-width="120" />
        <el-table-column prop="address" label="地址" width="180" show-overflow-tooltip />
        <el-table-column label="门票" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.ticketPrice > 0 ? 'warning' : 'success'" size="small">
              {{ row.ticketPrice > 0 ? `¥${row.ticketPrice}` : '免费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="onAttractionDblClick(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="attractionSelectorVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedAttraction" @click="addSelectedAttraction">
            {{ isSelectingForEdit ? '更新景点' : '添加选中项' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 酒店选择器弹窗 -->
    <el-dialog v-model="hotelSelectorVisible" title="从酒店库选择" width="720px" top="8vh">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #default>
          <div style="font-size:13px">
            <el-icon style="vertical-align: middle;"><InfoFilled /></el-icon>
            选择酒店后将自动填充酒店名称和描述
          </div>
        </template>
      </el-alert>
      <el-input v-model="hotelSearch" placeholder="搜索酒店名称..." prefix-icon="Search" style="margin-bottom:16px" clearable />
      <el-table :data="filteredHotels" max-height="400" highlight-current-row @current-change="onHotelSelect" @row-dblclick="onHotelDblClick" empty-text="暂无匹配的酒店">
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image :src="row.cover || row.image" fit="cover" style="width:60px;height:45px;border-radius:4px" lazy />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="酒店名称" min-width="120" />
        <el-table-column prop="address" label="地址" width="180" show-overflow-tooltip />
        <el-table-column label="价格" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.price > 0 ? 'warning' : 'success'" size="small">
              {{ row.price > 0 ? '¥' + row.price : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="onHotelDblClick(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="hotelSelectorVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedHotel" @click="addSelectedHotel">
            {{ isSelectingHotelForEdit ? '更新酒店' : '添加选中项' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 自定义路线规划编辑弹窗 -->
    <el-dialog v-model="journeyDialogVisible" :title="isJourneyEdit ? '编辑自定义规划' : '自定义路线规划详情'" width="720px">
      <div v-if="currentJourney">
        <!-- 封面图片 -->
        <div class="journey-cover-preview" v-if="getJourneyCover(currentJourney)">
          <el-image 
            :src="getJourneyCover(currentJourney)" 
            fit="cover" 
            class="journey-cover-img"
            :fallback-src="'https://via.placeholder.com/640x320/4A90E2/ffffff?text=NO+IMG'"
          />
        </div>
        
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规划名称" :span="2">
            <span v-if="!isJourneyEdit" style="font-weight:600;color:#1D3B5A;font-size:16px">{{ currentJourney.name }}</span>
            <el-input v-else v-model="currentJourney.name" />
          </el-descriptions-item>
          <el-descriptions-item label="用户 ID">{{ currentJourney.userId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="城市">
            <span v-if="!isJourneyEdit">{{ currentJourney.city || '-' }}</span>
            <el-input v-else v-model="currentJourney.city" />
          </el-descriptions-item>
          <el-descriptions-item label="天数">
            <span v-if="!isJourneyEdit">{{ currentJourney.days || 0 }}天</span>
            <el-input-number v-else v-model="currentJourney.days" :min="1" style="width:120px" />
          </el-descriptions-item>
          <el-descriptions-item label="人数">
            <span v-if="!isJourneyEdit">{{ currentJourney.people || 1 }}人</span>
            <el-input-number v-else v-model="currentJourney.people" :min="1" style="width:120px" />
          </el-descriptions-item>
          <el-descriptions-item label="预估费用">
            <span style="color:#A33B32;font-weight:bold;font-size:16px">¥{{ currentJourney.estimatedCost || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span v-if="!isJourneyEdit">
              <el-tag :type="journeyStatusType(currentJourney.status)" size="small">{{ currentJourney.status || '未开始' }}</el-tag>
            </span>
            <el-select v-else v-model="currentJourney.status" style="width:120px">
              <el-option label="未开始" value="未开始" />
              <el-option label="进行中" value="进行中" />
              <el-option label="已结束" value="已结束" />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="currentJourney.isCustom ? 'warning' : 'info'" size="small">{{ currentJourney.isCustom ? '自定义' : '推荐' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            <span v-if="!isJourneyEdit">{{ currentJourney.difficulty || '自定义' }}</span>
            <el-select v-else v-model="currentJourney.difficulty" style="width:120px">
              <el-option label="简单" value="简单" />
              <el-option label="中等" value="中等" />
              <el-option label="困难" value="困难" />
              <el-option label="自定义" value="自定义" />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="距离">{{ currentJourney.distance || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            <span v-if="!isJourneyEdit">{{ currentJourney.notes || '-' }}</span>
            <el-input v-else v-model="currentJourney.notes" type="textarea" :rows="2" />
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentJourney.createTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 行程站点 -->
        <div class="detail-section" v-if="currentStops.length > 0">
          <h4 class="detail-section-title">行程安排</h4>
          <div class="stops-timeline">
            <div class="stop-item" v-for="(stop, idx) in currentStops" :key="idx">
              <div class="stop-left">
                <div class="stop-num">{{ idx + 1 }}</div>
                <div class="stop-line" v-if="idx < currentStops.length - 1"></div>
              </div>
              <div class="stop-body">
                <div class="stop-header">
                  <span class="stop-name">{{ stop.name || '未知景点' }}</span>
                  <span class="stop-time" v-if="stop.time">{{ stop.time }}</span>
                </div>
                <div class="stop-desc" v-if="stop.desc">{{ stop.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 费用明细 -->
        <div class="detail-section" v-if="currentCostBreakdown">
          <h4 class="detail-section-title">费用明细</h4>
          <div class="cost-breakdown">
            <div class="cost-row" v-if="currentCostBreakdown.ticket">
              <span class="cost-label">景点门票</span>
              <span class="cost-value">¥{{ currentCostBreakdown.ticket }}</span>
            </div>
            <div class="cost-row" v-if="currentCostBreakdown.transport">
              <span class="cost-label">交通费用</span>
              <span class="cost-value">¥{{ currentCostBreakdown.transport }}</span>
            </div>
            <div class="cost-row" v-if="currentCostBreakdown.hotel">
              <span class="cost-label">住宿费用</span>
              <span class="cost-value">¥{{ currentCostBreakdown.hotel }}</span>
            </div>
            <div class="cost-row" v-if="currentCostBreakdown.meal">
              <span class="cost-label">餐饮费用</span>
              <span class="cost-value">¥{{ currentCostBreakdown.meal }}</span>
            </div>
            <div class="cost-row cost-total">
              <span class="cost-label">合计</span>
              <span class="cost-value">¥{{ currentCostBreakdown.total || currentJourney.estimatedCost }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="button-row">
          <el-button @click="journeyDialogVisible = false">关闭</el-button>
          <el-button v-if="!isJourneyEdit" type="primary" plain @click="startEditJourney">编辑</el-button>
          <el-button v-if="isJourneyEdit" type="primary" @click="handleSaveJourney">保存</el-button>
          <el-button v-if="isJourneyEdit" @click="cancelEditJourney">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Guide, Timer, MapLocation, InfoFilled, List, Clock, Coin, Top, Bottom, Edit, Delete, Rank, Check, UploadFilled, MagicStick, Picture, ZoomIn } from '@element-plus/icons-vue'
import { routeApi, userRouteApi, mapApi, attractionApi, hotelApi } from '@/api'

const loading = ref(false)
const activeTab = ref('routes')

// 景点和酒店数据用于获取封面
const attractionsMap = ref(new Map())
const hotelsMap = ref(new Map())

// 路线
const routeList = ref([])
const routeSearch = reactive({ keyword: '', difficulty: '' })
const routeDialogVisible = ref(false)
const isRouteEdit = ref(false)
const routeForm = ref({ id: '', name: '', difficulty: '', distance: 0, days: 1, duration: '', price: 0, cover: '', tags: '', stops: '', status: 1 })
const routeFormRef = ref(null)
const routeTags = ref([])
const newRouteTag = ref('')
const routeStops = ref([])
const stopDialogVisible = ref(false)
const editingStop = ref({ name: '', time: '', duration: '', desc: '', transport: '', ticketPrice: 0, transportCost: 0 })
const editingStopIdx = ref(-1)
const stopFormRef = ref(null)
const draggingIndex = ref(-1)
const savingRoute = ref(false)
const durationUnit = ref('小时')

// 景点选择器
const attractionSelectorVisible = ref(false)
const attractionSearch = ref('')
const selectedAttraction = ref(null)
const allAttractions = ref([])
const isSelectingForEdit = ref(false)  // 标记是否为编辑模式选择景点

// 酒店选择器
const hotelSelectorVisible = ref(false)
const hotelSearch = ref('')
const selectedHotel = ref(null)
const allHotels = ref([])
const isSelectingHotelForEdit = ref(false)  // 标记是否为编辑模式选择酒店

const quickRouteTags = ['热门', '一日游', '经典', '亲子', '摄影', '深度游', '文化体验', '自然风光', '休闲']

// 表单验证规则
const routeRules = {
  name: [
    { required: true, message: '请输入路线名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ],
  distance: [
    { required: true, message: '请输入距离', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

const stopRules = {
  name: [
    { required: true, message: '请输入景点名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在1到50个字符之间', trigger: 'blur' }
  ]
}

// 计算属性
const filteredAttractions = computed(() => {
  const list = !attractionSearch.value
    ? allAttractions.value.slice(0, 50)
    : allAttractions.value.filter(a => a.name.toLowerCase().includes(attractionSearch.value.toLowerCase()))

  // 映射 price 字段为 ticketPrice，确保显示正确
  return list.map(a => ({
    ...a,
    ticketPrice: a.price || 0
  }))
})

const calcTotalDuration = computed(() => {
  const total = routeStops.value.reduce((sum, s) => {
    const d = parseFloat(s.duration) || 0
    return sum + d
  }, 0)
  if (total === 0) return '未设置'
  if (total < 1) return `${Math.round(total * 60)}分钟`
  return `${total.toFixed(1)}小时`
})

const uniqueTransports = computed(() => {
  const set = new Set()
  routeStops.value.forEach(s => { if (s.transport) set.add(s.transport) })
  return [...set]
})

const totalTicketPrice = computed(() => {
  return routeStops.value.reduce((sum, s) => sum + (s.ticketPrice || 0), 0)
})

const totalHotelPrice = computed(() => {
  return routeStops.value.reduce((sum, s) => sum + (s.hotelPrice || 0), 0)
})

const totalTransportCost = computed(() => {
  return routeStops.value.reduce((sum, s) => sum + (s.transportCost || 0), 0)
})

const filteredRoutes = computed(() => {
  let data = routeList.value
  if (routeSearch.keyword) data = data.filter(r => r.name.includes(routeSearch.keyword))
  if (routeSearch.difficulty) data = data.filter(r => r.difficulty === routeSearch.difficulty)
  return data
})

// 自定义路线规划
const journeyList = ref([])
const journeySearch = reactive({ keyword: '', userId: '', status: '', isCustom: '' })
const journeyDialogVisible = ref(false)
const currentJourney = ref(null)
const isJourneyEdit = ref(false)
const journeyBackup = ref(null)
const journeyPage = ref(1)
const journeySize = ref(10)
const journeyTotal = ref(0)

const currentStops = computed(() => {
  if (!currentJourney.value?.stops) return []
  return parseJsonField(currentJourney.value.stops, [])
})

const currentCostBreakdown = computed(() => {
  if (!currentJourney.value?.costBreakdown) return null
  return parseJsonField(currentJourney.value.costBreakdown, null)
})

function parseJsonField(val, fallback) {
  if (!val) return fallback
  if (typeof val === 'object') return val
  try { return JSON.parse(val) } catch { return fallback }
}

function transportLabel(t) {
  const map = { driving: '🚗 自驾', drive: ' 自驾', bus: '🚌 公交', walking: '🚶 步行', walk: '🚶 步行', cablecar: '🚡 缆车' }
  return map[t] || t || '-'
}

// 加载景点和酒店数据
async function loadAttractionsAndHotels() {
  try {
    const [attractionRes, hotelRes] = await Promise.all([
      attractionApi.getAll(1, 200),
      hotelApi.getAll(1, 200)
    ])
    
    // 建立景点名称到封面的映射
    const attractions = attractionRes.data?.records || attractionRes.data?.list || []
    attractions.forEach(a => {
      attractionsMap.value.set(a.name, a.cover || a.image || '')
    })
    
    // 建立酒店名称到封面的映射
    const hotels = hotelRes.data?.records || hotelRes.data?.list || []
    hotels.forEach(h => {
      hotelsMap.value.set(h.name, h.cover || h.image || '')
    })
  } catch (e) {
    console.error('加载景点酒店数据失败:', e)
  }
}

// 获取自定义路线规划的封面
function getJourneyCover(journey) {
  // 如果已有封面，直接返回
  if (journey.cover) {
    return journey.cover
  }

  // 解析景点数据
  let attractions = []
  try {
    if (typeof journey.attractions === 'string') {
      attractions = JSON.parse(journey.attractions) || []
    } else if (Array.isArray(journey.attractions)) {
      attractions = journey.attractions
    }
  } catch {
    attractions = []
  }

  // 尝试从 attractions 中获取第一个景点的封面
  if (attractions.length > 0) {
    const firstAttraction = attractions[0]
    const attractionName = firstAttraction.name || firstAttraction
    const cover = attractionsMap.value.get(attractionName)
    if (cover) {
      return cover
    }
  }

  // 解析 daily 数据并尝试从景点中获取封面
  let stops = []
  try {
    // 先尝试 stops 字段
    if (typeof journey.stops === 'string') {
      stops = JSON.parse(journey.stops) || []
    } else if (Array.isArray(journey.stops)) {
      stops = journey.stops
    }
  } catch {
    stops = []
  }

  // 如果 stops 为空，尝试从 daily 中解析
  if (stops.length === 0 && journey.daily) {
    try {
      const dailyData = typeof journey.daily === 'string' ? JSON.parse(journey.daily) : journey.daily
      stops = dailyData.flatMap(day => day.stops || [])
    } catch {
      stops = []
    }
  }

  // 从 stops 中获取第一个景点/酒店的封面
  if (stops.length > 0) {
    for (const stop of stops) {
      if (stop.name || stop) {
        const cover = attractionsMap.value.get(stop.name || stop)
        if (cover) {
          return cover
        }
        // 尝试从酒店获取
        const hotelCover = hotelsMap.value.get(stop.name || stop)
        if (hotelCover) {
          return hotelCover
        }
      }
    }
  }

  // 默认返回空字符串，前端可以添加默认图片
  return ''
}

// 增强的 journeyStatusType 函数支持数字状态
function journeyStatusType(s) {
  if (s === 1) return 'success'
  if (s === 0) return 'info'
  return { '未开始': 'info', '进行中': 'warning', '已结束': 'success' }[s] || ''
}

// 获取景点数量
function getJourneyStopCount(journey) {
  if (journey.daily) {
    try {
      const dailyData = typeof journey.daily === 'string' ? JSON.parse(journey.daily) : journey.daily
      return dailyData.reduce((total, day) => total + (day.stops?.length || 0), 0)
    } catch {
      // 继续尝试从 stops 解析
    }
  }
  return parseJsonField(journey.stops, []).length
}

// 解析 budget 为费用
function parseJourneyBudget(budget) {
  if (typeof budget === 'string') {
    const match = budget.match(/\d+/)
    return match ? parseInt(match[0]) : 0
  }
  return budget || 0
}


async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'routes') {
      const res = await routeApi.getAll(1, 100, '', '', true)
      routeList.value = res.data?.records || res.data?.list || []
    } else if (activeTab.value === 'journeys') {
      // 构建筛选参数
      const keyword = journeySearch.keyword || ''
      const userId = journeySearch.userId ? parseInt(journeySearch.userId) : null
      const status = journeySearch.status || ''

      const res = await userRouteApi.getAll(journeyPage.value, journeySize.value, keyword, userId, status)
      journeyList.value = res.data?.list || res.data?.records || []
      journeyTotal.value = res.data?.total || 0
    }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

// 自定义路线规划操作
function handleAdd() {
  if (activeTab.value === 'routes') {
    isRouteEdit.value = false
    routeForm.value = { id: '', name: '', difficulty: '简单', distance: 0, days: 1, duration: '', price: 0, cover: '', tags: '[]', stops: '[]', status: 1 }
    routeTags.value = []
    routeStops.value = []
    newRouteTag.value = ''
    routeDialogVisible.value = true
    loadAllAttractions()
    loadAllHotels()
  } else {
    isJourneyEdit.value = false
    currentJourney.value = {
      id: '', name: '', userId: '', city: '三江', days: 1, people: 1,
      status: '未开始', isCustom: true, difficulty: '自定义', distance: '',
      notes: '', estimatedCost: 0, stops: '[]', tags: '[]',
      costBreakdown: '{}', hotelInfo: '', attractionIds: ''
    }
    journeyBackup.value = null
    journeyDialogVisible.value = true
  }
}

function handleEditRoute(row) {
  isRouteEdit.value = true
  routeForm.value = { id: '', name: '', difficulty: '简单', distance: 0, days: 1, duration: '', price: 0, cover: '', tags: '[]', stops: '[]', status: 1, ...row }
  routeTags.value = parseJsonField(row.tags, [])
  routeStops.value = parseJsonField(row.stops, [])
  newRouteTag.value = ''
  routeDialogVisible.value = true
  loadAllAttractions()
  loadAllHotels()
}

async function handleSaveRoute() {
  try {
    // 表单验证
    if (!routeForm.value.name || routeForm.value.name.trim().length < 2) {
      ElMessage.warning('请输入至少2个字符的路线名称')
      return
    }

    if (routeFormRef.value) {
      try {
        await routeFormRef.value.validate()
      } catch (validateError) {
        ElMessage.warning('请检查表单填写是否完整')
        return
      }
    }

    savingRoute.value = true

    // 构建保存数据
    const saveData = {
      name: routeForm.value.name.trim(),
      difficulty: routeForm.value.difficulty || '简单',
      distance: routeForm.value.distance || 0,
      duration: routeForm.value.duration || '',
      price: routeForm.value.price || 0,
      cover: routeForm.value.cover || '',
      status: routeForm.value.status ?? 1,
      tags: JSON.stringify(routeTags.value),
      stops: JSON.stringify(routeStops.value)
    }

    // 如果是编辑模式，添加 id
    if (isRouteEdit.value && routeForm.value.id) {
      saveData.id = routeForm.value.id
    }

    // 调用 API
    if (saveData.id) {
      await routeApi.update(saveData)
      ElMessage.success('路线更新成功')
    } else {
      await routeApi.save(saveData)
      ElMessage.success('路线创建成功')
    }

    routeDialogVisible.value = false
    await loadData()
  } catch (error) {
    console.error('保存路线失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '保存失败'
    ElMessage.error(errorMsg)
  } finally {
    savingRoute.value = false
  }
}

// 路线标签管理
function addRouteTag() {
  const tag = newRouteTag.value.trim()
  if (tag && !routeTags.value.includes(tag)) {
    routeTags.value.push(tag)
  }
  newRouteTag.value = ''
}

function quickAddRouteTag(tag) {
  if (!routeTags.value.includes(tag)) {
    routeTags.value.push(tag)
  }
}

function removeRouteTag(idx) {
  routeTags.value.splice(idx, 1)
}

// 路线站点管理
function addRouteStop() {
  editingStop.value = { name: '', time: '', duration: '', desc: '', transport: '', ticketPrice: 0, transportCost: 0 }
  editingStopIdx.value = -1
  stopDialogVisible.value = true
}

function editRouteStop(idx) {
  editingStopIdx.value = idx
  editingStop.value = { name: '', time: '', duration: '', desc: '', transport: '', ticketPrice: 0, transportCost: 0, ...routeStops.value[idx] }
  stopDialogVisible.value = true
}

function saveRouteStop() {
  if (!editingStop.value.name || editingStop.value.name.trim().length === 0) {
    ElMessage.warning('请输入景点名称')
    return
  }

  // 确保时长是数字格式
  if (editingStop.value.duration) {
    editingStop.value.duration = parseFloat(editingStop.value.duration) || 1
  }

  if (editingStopIdx.value === -1) {
    routeStops.value.push({ ...editingStop.value })
    ElMessage.success('站点添加成功')
  } else {
    routeStops.value[editingStopIdx.value] = { ...editingStop.value }
    ElMessage.success('站点更新成功')
  }
  stopDialogVisible.value = false
}

function removeRouteStop(idx) {
  routeStops.value.splice(idx, 1)
}

function moveRouteStopUp(idx) {
  if (idx <= 0) return
  const temp = routeStops.value[idx]
  routeStops.value[idx] = routeStops.value[idx - 1]
  routeStops.value[idx - 1] = temp
  routeStops.value = [...routeStops.value]
}

function moveRouteStopDown(idx) {
  if (idx >= routeStops.value.length - 1) return
  const temp = routeStops.value[idx]
  routeStops.value[idx] = routeStops.value[idx + 1]
  routeStops.value[idx + 1] = temp
  routeStops.value = [...routeStops.value]
}

// 拖拽排序
function onDragStart(idx) {
  draggingIndex.value = idx
}

function onDragEnd() {
  draggingIndex.value = -1
  routeStops.value = [...routeStops.value]
}

function onDragOver(idx) {
  if (draggingIndex.value === -1 || draggingIndex.value === idx) return
  
  const dragItem = routeStops.value[draggingIndex.value]
  const targetItem = routeStops.value[idx]
  routeStops.value[draggingIndex.value] = targetItem
  routeStops.value[idx] = dragItem
  draggingIndex.value = idx
}

function onDrop(idx) {
  draggingIndex.value = -1
}

function transportType(t) {
  const map = { walking: 'success', bus: 'primary', driving: 'warning', cablecar: 'danger', drive: 'warning', walk: 'success' }
  return map[t] || 'info'
}

// 交通方式费用映射
const transportCostMap = {
  walking: 0,
  bus: 2,
  driving: 10
}

// 交通方式改变时自动设置费用
function onTransportChange(stop) {
  if (stop.transport && transportCostMap[stop.transport] !== undefined) {
    stop.transportCost = transportCostMap[stop.transport]
  }
}

// 自动计算价格
function autoCalcPrice() {
  if (routeStops.value.length === 0) {
    ElMessage.warning('请先添加站点')
    return
  }

  const calculatedPrice = totalTicketPrice.value + totalHotelPrice.value + totalTransportCost.value
  routeForm.value.price = calculatedPrice

  ElMessage.success(`已自动计算价格：¥${calculatedPrice} (门票¥${totalTicketPrice.value} + 酒店¥${totalHotelPrice.value} + 交通¥${totalTransportCost.value})`)
}

// 景点选择器
async function loadAllAttractions() {
  try {
    const res = await attractionApi.getAll(1, 500)
    allAttractions.value = res.data?.records || res.data?.list || []
  } catch (e) {
    console.error('加载景点列表失败:', e)
  }
}

function openAttractionSelector() {
  attractionSearch.value = ''
  selectedAttraction.value = null
  attractionSelectorVisible.value = true
  isSelectingForEdit.value = false  // 标记为添加模式
  if (allAttractions.value.length === 0) {
    loadAllAttractions()
  }
}

function openAttractionSelectorForEdit() {
  attractionSearch.value = ''
  selectedAttraction.value = null
  attractionSelectorVisible.value = true
  isSelectingForEdit.value = true  // 标记为编辑模式
  if (allAttractions.value.length === 0) {
    loadAllAttractions()
  }
}

function onAttractionSelect(row) {
  selectedAttraction.value = row
}

function onAttractionDblClick(row) {
  selectedAttraction.value = row
  addSelectedAttraction()
}

function addSelectedAttraction() {
  if (!selectedAttraction.value) {
    ElMessage.warning('请选择一个景点')
    return
  }
  const a = selectedAttraction.value

  // 如果是编辑模式，更新当前编辑的站点信息
  if (isSelectingForEdit.value && editingStop.value) {
    editingStop.value.name = a.name
    editingStop.value.desc = a.description || ''
    editingStop.value.ticketPrice = a.price || a.ticketPrice || 0
    attractionSelectorVisible.value = false
    selectedAttraction.value = null
    ElMessage.success(`已更新景点信息`)
    return
  }

  // 添加模式：创建新站点
  const nextTime = routeStops.value.length > 0 ? routeStops.value[routeStops.value.length - 1].time : '08:00'
  const newStop = {
    name: a.name,
    time: nextTime,
    duration: '1',
    desc: a.description || '',
    transport: 'bus',
    transportCost: 2,
    ticketPrice: a.price || a.ticketPrice || 0  // 优先使用 price 字段
  }
  routeStops.value.push(newStop)
  attractionSelectorVisible.value = false
  selectedAttraction.value = null
  ElMessage.success(`已添加 "${a.name}"`)
}

// 景点封面选择器
const coverSelectorVisible = ref(false)

function openAttractionCoverSelector() {
  coverSelectorVisible.value = true
  if (allAttractions.value.length === 0) {
    loadAllAttractions()
  }
}

function selectAttractionCover(attraction) {
  if (attraction.cover || attraction.image) {
    routeForm.value.cover = attraction.cover || attraction.image
    coverSelectorVisible.value = false
    ElMessage.success(`已选择 "${attraction.name}" 的封面`)
  } else {
    ElMessage.warning('该景点没有封面图片')
  }
}

function previewCover() {
  if (routeForm.value.cover) {
    window.open(routeForm.value.cover, '_blank')
  }
}

// 图片选择器（保留兼容性）
function openImagePicker(type) {
  openAttractionCoverSelector()
}

// 酒店选择器
async function loadAllHotels() {
  try {
    const res = await hotelApi.getAll(1, 500)
    allHotels.value = res.data?.records || res.data?.list || []
  } catch (e) {
    console.error('加载酒店列表失败:', e)
  }
}

const filteredHotels = computed(() => {
  const list = !hotelSearch.value
    ? allHotels.value.slice(0, 50)
    : allHotels.value.filter(h => h.name.toLowerCase().includes(hotelSearch.value.toLowerCase()))
  return list
})

function openHotelSelector() {
  hotelSearch.value = ''
  selectedHotel.value = null
  hotelSelectorVisible.value = true
  isSelectingHotelForEdit.value = false
  if (allHotels.value.length === 0) {
    loadAllHotels()
  }
}

function openHotelSelectorForEdit() {
  hotelSearch.value = ''
  selectedHotel.value = null
  hotelSelectorVisible.value = true
  isSelectingHotelForEdit.value = true
  if (allHotels.value.length === 0) {
    loadAllHotels()
  }
}

function onHotelSelect(row) {
  selectedHotel.value = row
}

function onHotelDblClick(row) {
  selectedHotel.value = row
  addSelectedHotel()
}

function addSelectedHotel() {
  if (!selectedHotel.value) {
    ElMessage.warning('请选择一个酒店')
    return
  }
  const h = selectedHotel.value

  // 如果是编辑模式，更新当前编辑的站点信息
  if (isSelectingHotelForEdit.value && editingStop.value) {
    editingStop.value.name = h.name
    editingStop.value.desc = h.description || ''
    hotelSelectorVisible.value = false
    selectedHotel.value = null
    ElMessage.success(`已更新酒店信息`)
    return
  }

  // 添加模式：创建新站点
  const nextTime = routeStops.value.length > 0 ? routeStops.value[routeStops.value.length - 1].time : '18:00'
  const newStop = {
    name: h.name,
    time: nextTime,
    duration: '8',
    desc: h.description || '',
    transport: 'bus',
    transportCost: 2,
    hotelPrice: h.price || 0,  // 保存酒店价格
    ticketPrice: 0,
    type: 'hotel'
  }
  routeStops.value.push(newStop)
  hotelSelectorVisible.value = false
  selectedHotel.value = null
  ElMessage.success(`已添加酒店 "${h.name}"`)
}

// 更新时长显示
function updateDuration() {
  if (editingStop.value.duration) {
    editingStop.value.duration = `${editingStop.value.duration}${durationUnit.value}`
  }
}

async function handleDeleteRoute(row) {
  await ElMessageBox.confirm('确认删除该路线？', '提示', { type: 'warning' })
  try {
    await routeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { ElMessage.error('删除失败') }
}

async function handleToggleRouteStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await routeApi.update({ ...row, status: newStatus })
    ElMessage.success(`${action}成功！`)
    loadData()
  } catch { ElMessage.error(`${action}失败`) }
}

// 自定义路线规划操作
function handleEditJourney(row) {
  isJourneyEdit.value = false
  currentJourney.value = { ...row }
  journeyBackup.value = null
  journeyDialogVisible.value = true
}

function startEditJourney() {
  journeyBackup.value = { ...currentJourney.value }
  isJourneyEdit.value = true
}

function cancelEditJourney() {
  if (journeyBackup.value) {
    currentJourney.value = { ...journeyBackup.value }
  }
  isJourneyEdit.value = false
  journeyBackup.value = null
}

async function handleSaveJourney() {
  try {
    if (!currentJourney.value.name) {
      ElMessage.warning('请输入规划名称')
      return
    }
    await userRouteApi.update(currentJourney.value)
    ElMessage.success('保存成功')
    isJourneyEdit.value = false
    journeyBackup.value = null
    loadData()
  } catch { ElMessage.error('保存失败') }
}

async function handleDeleteJourney(row) {
  await ElMessageBox.confirm('确认删除该自定义规划？', '提示', { type: 'warning' })
  try {
    await userRouteApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { ElMessage.error('删除失败') }
}

// ===== 地图相关 =====
const mapFilters = reactive({ attractions: true, heritages: true, activities: true })
const mapData = reactive({ attractions: [], heritages: [], activities: [], center: { lat: 25.7750, lng: 109.6030 } })
let mapInstance = null
let mapMarkers = []

function createCustomIcon(type) {
  const colors = { attraction: '#4A90E2', heritage: '#D4A373', activity: '#A33B32' }
  const labels = { attraction: '景', heritage: '遗', activity: '活' }
  const color = colors[type] || '#4A90E2'
  const label = labels[type] || '?'
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="width:32px;height:32px;border-radius:50%;background:${color};color:#fff;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;box-shadow:0 2px 8px rgba(0,0,0,0.3);border:2px solid #fff">${label}</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  })
}

function addMarkersToMap() {
  if (!mapInstance) return
  mapMarkers.forEach(m => mapInstance.removeLayer(m))
  mapMarkers = []

  if (mapFilters.attractions) {
    mapData.attractions.forEach(item => {
      const marker = L.marker([item.lat, item.lng], { icon: createCustomIcon('attraction') })
        .bindPopup(`<b style="color:#4A90E2">景点</b><br/><strong>${item.name}</strong><br/>${item.address || ''}<br/>${item.price ? '¥' + item.price : ''}`)
      marker.addTo(mapInstance)
      mapMarkers.push(marker)
    })
  }
  if (mapFilters.heritages) {
    mapData.heritages.forEach(item => {
      const marker = L.marker([item.lat, item.lng], { icon: createCustomIcon('heritage') })
        .bindPopup(`<b style="color:#D4A373">非遗</b><br/><strong>${item.name}</strong><br/>${item.level || ''}<br/>${item.description ? item.description.substring(0, 60) : ''}`)
      marker.addTo(mapInstance)
      mapMarkers.push(marker)
    })
  }
  if (mapFilters.activities) {
    mapData.activities.forEach(item => {
      const marker = L.marker([item.lat, item.lng], { icon: createCustomIcon('activity') })
        .bindPopup(`<b style="color:#A33B32">活动</b><br/><strong>${item.title}</strong><br/>${item.location || ''}<br/>${item.status || ''}`)
      marker.addTo(mapInstance)
      mapMarkers.push(marker)
    })
  }
}

function refreshMapMarkers() {
  addMarkersToMap()
}

async function initMap() {
  // 等待 DOM 渲染
  await new Promise(r => setTimeout(r, 100))
  const container = document.getElementById('sanjiang-map')
  if (!container) return

  if (mapInstance) {
    mapInstance.invalidateSize()
    return
  }

  mapInstance = L.map('sanjiang-map', { zoomControl: true }).setView([mapData.center.lat, mapData.center.lng], 13)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(mapInstance)

  try {
    const res = await mapApi.getData()
    const data = res.data || {}
    mapData.attractions = data.attractions || []
    mapData.heritages = data.heritages || []
    mapData.activities = data.activities || []
    if (data.center) mapData.center = data.center
    mapInstance.setView([mapData.center.lat, mapData.center.lng], 13)
    addMarkersToMap()
  } catch (e) {
    console.error('加载地图数据失败', e)
    ElMessage.error('加载地图数据失败')
  }
}

// 初始化时加载景点和酒店数据
onMounted(() => {
  loadData()
  loadAttractionsAndHotels()
})
</script>

<style scoped lang="scss">
.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.gradient-text { background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }

.routes-page { position: relative; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 26px; font-weight: 800; margin-bottom: 4px; }
.page-sub { color: #999; font-size: 14px; }
.btn-glow { position: relative; overflow: hidden; }

// Tab
.tab-bar { padding: 12px 18px; margin-bottom: 16px; }
.tab-group { display: flex; gap: 4px; }
.tab-item { padding: 10px 20px; font-size: 14px; font-weight: 500; color: #666; border-radius: 10px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 8px; &:hover { color: #1D3B5A; background: rgba(29,59,90,0.05); } &.active { color: #A33B32; background: rgba(163,59,50,0.08); font-weight: 600; } }
.tab-count { font-size: 11px; background: rgba(0,0,0,0.06); padding: 2px 8px; border-radius: 10px; }

// 搜索
.search-bar { padding: 14px 18px; margin-bottom: 16px; display: flex; align-items: center; }

// 表格
.table-card { padding: 20px; }
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
}
.action-edit { background: linear-gradient(135deg, #1D3B5A, #2E5F8E) !important; border-color: #1D3B5A !important; color: white !important; &:hover { background: linear-gradient(135deg, #2E5F8E, #1D3B5A) !important; } }
.action-delete { background: linear-gradient(135deg, #A33B32, #C94A40) !important; border-color: #A33B32 !important; color: white !important; &:hover { background: linear-gradient(135deg, #C94A40, #A33B32) !important; } }

// 响应式
@media screen and (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .search-bar { flex-wrap: wrap; gap: 8px; }
}

// 详情弹窗 - 行程站点
.detail-section { margin-top: 20px; padding-top: 16px; border-top: 1px solid #EBEEF5; }
.detail-section-title { font-size: 15px; font-weight: 600; color: #1D3B5A; margin-bottom: 16px; }

// 自定义路线规划封面预览
.journey-cover-preview { margin-bottom: 20px; border-radius: 12px; overflow: hidden; }
.journey-cover-img { width: 100%; height: 320px; display: block; background: #F5F7FA; }

.stops-timeline { display: flex; flex-direction: column; gap: 0; }
.stop-item { display: flex; gap: 12px; }
.stop-left { display: flex; flex-direction: column; align-items: center; width: 32px; flex-shrink: 0; }
.stop-num { width: 28px; height: 28px; border-radius: 50%; background: #4A90E2; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
.stop-line { flex: 1; width: 2px; background: #E4E7ED; margin: 4px 0; min-height: 24px; }
.stop-body { flex: 1; padding-bottom: 16px; }
.stop-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.stop-name { font-size: 14px; font-weight: 600; color: #303133; }
.stop-time { font-size: 12px; color: #E6A23C; font-weight: 500; }
.stop-meta { display: flex; gap: 16px; margin-bottom: 4px; }
.stop-duration { font-size: 12px; color: #909399; }
.stop-desc { font-size: 13px; color: #606266; line-height: 1.5; }
.stop-actions-row { margin-top: 6px; display: flex; gap: 4px; }
.stop-transport-selector { display: flex; align-items: center; }

// 费用明细
.cost-breakdown { background: #FAFAFA; border-radius: 8px; padding: 12px 16px; }
.cost-row { display: flex; justify-content: space-between; padding: 6px 0; }
.cost-label { font-size: 13px; color: #606266; }
.cost-value { font-size: 13px; color: #303133; font-weight: 600; }
.cost-total { border-top: 1px dashed #DCDFE6; margin-top: 8px; padding-top: 10px; }
.cost-total .cost-label { font-weight: 600; color: #1D3B5A; }
.cost-total .cost-value { font-size: 16px; color: #A33B32; font-weight: 700; }

// 地图
.map-toolbar { padding: 14px 18px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.map-legend { display: flex; align-items: center; gap: 16px; font-size: 13px; color: #606266; }
.legend-title { font-weight: 600; color: #303133; }
.legend-item { display: flex; align-items: center; gap: 6px; }
.legend-dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; border: 2px solid #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.2); }
.map-filters { display: flex; gap: 16px; }
.map-container { height: 600px; border-radius: 16px; overflow: hidden; border: 1px solid rgba(0,0,0,0.06); }
:deep(.custom-marker) { background: transparent !important; border: none !important; }
:deep(.leaflet-popup-content-wrapper) { border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
:deep(.leaflet-popup-content) { font-size: 13px; line-height: 1.6; margin: 10px 14px; }

// 站点编辑弹窗样式
.stop-edit-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #36CFC9 0%, #409EFF 100%);
    color: #FFFFFF;
    padding: 18px 24px;
    margin: 0;
    border-radius: 12px 12px 0 0;

    .el-dialog__title {
      color: #FFFFFF;
      font-size: 16px;
      font-weight: 600;
    }

    .el-dialog__headerbtn {
      top: 18px;

      .el-dialog__close {
        color: #FFFFFF;
        font-size: 18px;

        &:hover {
          color: #D4A373;
        }
      }
    }
  }

  .stop-form {
    padding: 24px;

    .transport-group {
      width: 100%;

      :deep(.el-radio-button) {
        flex: 1;

        .el-radio-button__inner {
          width: 100%;
        }
      }
    }
  }
}

// ===== 路线编辑弹窗样式 =====
.route-edit-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
    color: #FFFFFF;
    padding: 20px 24px;
    margin: 0;
    border-radius: 16px 16px 0 0;

    .el-dialog__title {
      color: #FFFFFF;
      font-size: 18px;
      font-weight: 600;
    }

    .el-dialog__headerbtn {
      top: 20px;

      .el-dialog__close {
        color: #FFFFFF;
        font-size: 20px;

        &:hover {
          color: #D4A373;
        }
      }
    }
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 0;
    border-top: 1px solid #EBEEF5;
  }
}

.route-form-container {
  max-height: 68vh;
  overflow-y: auto;
  padding: 24px;
  background: #FAFBFC;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #F5F7FA;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #409EFF, #36CFC9);
    border-radius: 4px;

    &:hover {
      background: linear-gradient(180deg, #36CFC9, #409EFF);
    }
  }
}

.form-section {
  margin-bottom: 20px;
  padding: 24px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #E4E7ED;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    border-color: #C0C4CC;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1D3B5A;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #E4E7ED;

  .el-icon {
    color: #409EFF;
    font-size: 18px;
  }
}

.section-actions {
  display: flex;
  gap: 8px;
}

.stop-count-tag {
  margin-left: 8px;
  font-weight: normal;
}

// 封面预览
.cover-upload-section {
  .cover-input-group {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .cover-preview-area {
    position: relative;
    width: 100%;
    max-width: 400px;
    border-radius: 8px;
    overflow: hidden;
    border: 2px solid #E4E7ED;
    transition: all 0.3s;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
    }

    .cover-preview-img {
      width: 100%;
      height: 200px;
      display: block;
      background: #F5F7FA;
    }

    .image-error {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;
      font-size: 14px;

      .el-icon {
        font-size: 48px;
        margin-bottom: 8px;
      }
    }

    .cover-preview-actions {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
      padding: 12px;
      display: flex;
      justify-content: center;
      gap: 16px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover .cover-preview-actions {
      opacity: 1;
    }
  }

  .cover-placeholder {
    width: 100%;
    max-width: 400px;
    height: 200px;
    border: 2px dashed #DCDFE6;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 14px;
    background: #FAFAFA;

    .el-icon {
      font-size: 48px;
      margin-bottom: 8px;
      opacity: 0.5;
    }
  }
}

.status-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

// 封面选择器网格
.cover-selector-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0,0,0,0.2);
    border-radius: 3px;
  }
}

.cover-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #E4E7ED;
  transition: all 0.3s;

  &:hover {
    border-color: #409EFF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }

  &.no-cover {
    opacity: 0.6;
    cursor: not-allowed;

    &:hover {
      transform: none;
      border-color: #E4E7ED;
      box-shadow: none;
    }
  }

  .cover-item-img {
    width: 100%;
    height: 120px;
    display: block;
    background: #F5F7FA;
  }

  .cover-item-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #C0C4CC;
    font-size: 12px;

    .el-icon {
      font-size: 32px;
      margin-bottom: 4px;
    }
  }

  .cover-item-name {
    padding: 8px;
    background: #FFFFFF;
    font-size: 12px;
    color: #303133;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-weight: 500;
  }
}

.cover-preview-wrapper {
  position: relative;
  display: inline-block;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.cover-preview-img {
  width: 300px;
  height: 180px;
  display: block;
}

.cover-preview-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0.8;
}

// 标签管理
.tags-manager {
  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-bottom: 12px;
  }
  
  .selected-tag {
    cursor: default;
  }
  
  .tag-input {
    width: 160px;
  }
  
  .quick-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
    padding-top: 12px;
    border-top: 1px dashed #DCDFE6;
  }
  
  .quick-tags-label {
    font-size: 12px;
    color: #909399;
    margin-right: 8px;
  }
  
  .quick-tag {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

// 行程统计卡片
.route-stats-card {
  margin-bottom: 20px;
  border-radius: 8px;
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      font-weight: 500;
    }
  }
}

// 站点列表
.stops-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stop-card {
  background: #FFFFFF;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #E4E7ED;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    border-color: #C0C4CC;
  }
  
  &.dragging {
    opacity: 0.6;
    border-style: dashed;
  }
  
  .stop-card-header {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .stop-number {
    flex-shrink: 0;
  }
  
  .stop-num-badge {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409EFF, #36CFC9);
    color: #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
  }
  
  .stop-info {
    flex: 1;
    min-width: 0;
  }
  
  .stop-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .stop-details {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .stop-detail-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #909399;
  }
  
  .stop-transport {
    flex-shrink: 0;
  }
  
  .stop-actions {
    display: flex;
    gap: 2px;
    flex-shrink: 0;
  }
  
  .drag-handle {
    cursor: move;
    
    &:hover {
      color: #409EFF;
    }
  }
  
  .stop-desc {
    margin-top: 10px;
    padding: 10px;
    background: #F5F7FA;
    border-radius: 6px;
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}

// 空状态
.empty-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

// 页脚
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: #FAFBFC;
  border-top: 1px solid #EBEEF5;
}

.save-btn {
  min-width: 140px;
  font-weight: 600;
  background: linear-gradient(135deg, #409EFF, #36CFC9);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #36CFC9, #409EFF);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}
</style>

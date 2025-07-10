package com.example.dockerapp.data.model

import com.google.gson.annotations.SerializedName

// Dashboard search response
data class GrafanaDashboard(
    val id: Int,
    val uid: String,
    val title: String,
    val uri: String,
    val url: String,
    val slug: String,
    val type: String,
    val tags: List<String>,
    val isStarred: Boolean,
    val folderId: Int?,
    val folderUid: String?,
    val folderTitle: String?,
    val folderUrl: String?
)

// Dashboard details
data class GrafanaDashboardDetail(
    val dashboard: Dashboard,
    val meta: DashboardMeta
)

data class Dashboard(
    val id: Int?,
    val uid: String,
    val title: String,
    val tags: List<String>,
    val timezone: String,
    val panels: List<Panel>,
    val time: TimeRange,
    val timepicker: TimePicker,
    val refresh: String,
    val schemaVersion: Int,
    val version: Int
)

data class DashboardMeta(
    val type: String,
    val canSave: Boolean,
    val canEdit: Boolean,
    val canAdmin: Boolean,
    val canStar: Boolean,
    val slug: String,
    val url: String,
    val expires: String,
    val created: String,
    val updated: String,
    val updatedBy: String,
    val createdBy: String,
    val version: Int,
    val hasAcl: Boolean,
    val isFolder: Boolean,
    val folderId: Int,
    val folderTitle: String,
    val folderUrl: String,
    val provisioned: Boolean,
    val provisionedExternalId: String
)

data class Panel(
    val id: Int,
    val title: String,
    val type: String,
    val targets: List<Target>,
    val gridPos: GridPos,
    val options: Map<String, Any>?,
    val fieldConfig: FieldConfig?,
    val transformations: List<Any>?
)

data class Target(
    val expr: String?,
    val refId: String,
    val intervalFactor: Int?,
    val step: Int?,
    val datasource: DataSource?
)

data class DataSource(
    val type: String,
    val uid: String
)

data class GridPos(
    val h: Int,
    val w: Int,
    val x: Int,
    val y: Int
)

data class FieldConfig(
    val defaults: FieldDefaults?,
    val overrides: List<Any>?
)

data class FieldDefaults(
    val color: Map<String, Any>?,
    val custom: Map<String, Any>?,
    val mappings: List<Any>?,
    val thresholds: Map<String, Any>?,
    val unit: String?
)

data class TimeRange(
    val from: String,
    val to: String
)

data class TimePicker(
    @SerializedName("refresh_intervals")
    val refreshIntervals: List<String>,
    @SerializedName("time_options")
    val timeOptions: List<String>
)

// Query API response
data class GrafanaQueryResponse(
    val data: List<QueryResult>
)

data class QueryResult(
    val target: String,
    val datapoints: List<List<Any>>,
    val tags: Map<String, String>?
)

// Data source
data class GrafanaDataSource(
    val id: Int,
    val uid: String,
    val name: String,
    val type: String,
    val url: String,
    val access: String,
    val isDefault: Boolean
)

// Health check
data class GrafanaHealth(
    val commit: String,
    val database: String,
    val version: String
)
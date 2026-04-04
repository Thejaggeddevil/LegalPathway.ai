package com.legalpathways.ai.network

import com.legalpathways.ai.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Chat & Counselor ─────────────────────────────────────────────────────
    @POST("ask")
    suspend fun ask(@Body request: AskRequest): Response<ApiResponse<AskData>>

    @POST("counselor-structured")
    suspend fun counselorChat(@Body request: CounselorRequest): Response<ApiResponse<CounselorData>>

    // ── Roadmap ──────────────────────────────────────────────────────────────
    @GET("roadmap")
    suspend fun getRoadmap(
        @Query("marriage") marriage: String,
        @Query("role") role: String
    ): Response<ApiResponse<RoadmapData>>

    // ── Layer 0 ──────────────────────────────────────────────────────────────
    @POST("api/level0/position")
    suspend fun getLayer0Position(@Body request: Layer0Request): Response<ApiResponse<Layer0Data>>

    // ── Layer 1 (Phase 1) ────────────────────────────────────────────────────
    @GET("api/phase1")
    suspend fun getPhase1(@Query("religion") religion: String): Response<ApiResponse<Phase1Data>>

    // ── Layer 2 ──────────────────────────────────────────────────────────────
    @GET("api/layer2/checklist")
    suspend fun getLayer2Checklist(): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("api/layer2/checklist")
    suspend fun getLayer2ChecklistDetail(
        @Query("act") act: String
    ): Response<ApiResponse<Map<String, Any>>>
    // ── Layer 3 ──────────────────────────────────────────────────────────────
    @GET("api/layer3/events")
    suspend fun getLayer3Events(): Response<ApiResponse<List<ScenarioItem>>>

    // ── Layer 4 ──────────────────────────────────────────────────────────────
    @POST("api/layer4/classify")
    suspend fun classifyLayer4(@Body request: Layer4Request): Response<ApiResponse<Layer4Data>>

    // ── Layer 5 ──────────────────────────────────────────────────────────────
    @POST("api/layer5/settlement")
    suspend fun getLayer5Settlement(@Body request: Layer5Request): Response<ApiResponse<Layer5Data>>

    // ── Layer 6 ──────────────────────────────────────────────────────────────
    @GET("api/layer6")
    suspend fun getLayer6(): Response<ApiResponse<Map<String, Any>>>

    // ── Layer 7 ──────────────────────────────────────────────────────────────
    @GET("api/layer7")
    suspend fun getLayer7(): Response<ApiResponse<Layer7Data>>

    // ── Layer 8 ──────────────────────────────────────────────────────────────
    @GET("api/layer8")
    suspend fun getLayer8(): Response<ApiResponse<Layer8Data>>

    // ── Layer 9 ──────────────────────────────────────────────────────────────
    @GET("api/layer9")
    suspend fun getLayer9(): Response<ApiResponse<Layer9Data>>

    // ── Layer 10 ─────────────────────────────────────────────────────────────
    @GET("api/layer10")
    suspend fun getLayer10(): Response<ApiResponse<Layer10Data>>
}
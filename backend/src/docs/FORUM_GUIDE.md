# Forum (Community) Android Integration Guide

- Base URL: <BASE_URL> (e.g., http://localhost:3000)
- Auth: Bearer JWT in Authorization header
- Swagger: <BASE_URL>/api/docs (Forums section)

## Features covered
- Create/get forum, follow/unfollow, join/leave
- Create/list posts (permissions: admin_only, followers, members)
- Nested comments via parentCommentId
- Per-user reactions (like/dislike) and share using bulk batching
- Owner-only role management (admin/moderator/member)

## Endpoints
- Forums
  - POST /api/forums
  - GET /api/forums/slug/{slug}
  - POST /api/forums/{forumId}/follow | /unfollow | /join | /leave
  - POST /api/forums/{forumId}/members/role
- Posts
  - POST /api/forums/{forumId}/posts
  - GET /api/forums/{forumId}/posts?page&limit
- Comments
  - POST /api/forums/posts/{postId}/comments
  - GET /api/forums/posts/{postId}/comments?parentCommentId&page&limit
- Bulk interactions
  - POST /api/forums/interactions/bulk

## Request payloads
- Create forum
{
  "name": "Gaming Talk",
  "slug": "gaming-talk",
  "description": "Casual gaming",
  "verified": false,
  "postPermission": "members"
}

- Create post
{ "title": "Best builds for X", "content": "Share your thoughts" }

- Add comment (root)
{ "content": "Nice!" }

- Add comment (reply)
{ "content": "Agree", "parentCommentId": "64f..." }

- Bulk interactions
{
  "operations": [
    { "op": "post_reaction", "postId": "64f...", "type": "like" },
    { "op": "comment_reaction", "commentId": "64c...", "type": "dislike" },
    { "op": "post_share", "postId": "64f..." }
  ]
}

- Change role (owner)
{ "userId": "64u...", "role": "admin" }

## Android Retrofit (Kotlin)
// build.gradle
dependencies {
  implementation "com.squareup.retrofit2:retrofit:2.11.0"
  implementation "com.squareup.retrofit2:converter-moshi:2.11.0"
  implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
}

// Retrofit setup
val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
val client = OkHttpClient.Builder()
  .addInterceptor(logging)
  .addInterceptor { chain ->
    val token = /* read from secure storage */
    val req = chain.request().newBuilder()
      .addHeader("Authorization", "Bearer $token")
      .addHeader("Content-Type", "application/json")
      .build()
    chain.proceed(req)
  }
  .build()
val retrofit = Retrofit.Builder()
  .baseUrl("<BASE_URL>/")
  .client(client)
  .addConverterFactory(MoshiConverterFactory.create())
  .build()

// DTOs
data class CreateForumBody(val name: String, val slug: String, val description: String? = null, val verified: Boolean? = null, val postPermission: String? = null)

data class CreatePostBody(val title: String, val content: String)

data class AddCommentBody(val content: String, val parentCommentId: String? = null)

data class BulkOp(val op: String, val postId: String? = null, val commentId: String? = null, val type: String? = null)

data class BulkBody(val operations: List<BulkOp>)

data class RoleBody(val userId: String, val role: String)

// API
interface ForumsApi {
  @POST("api/forums") suspend fun createForum(@Body body: CreateForumBody): ApiResp
  @GET("api/forums/slug/{slug}") suspend fun getForumBySlug(@Path("slug") slug: String): ApiResp
  @POST("api/forums/{forumId}/follow") suspend fun follow(@Path("forumId") forumId: String): ApiResp
  @POST("api/forums/{forumId}/unfollow") suspend fun unfollow(@Path("forumId") forumId: String): ApiResp
  @POST("api/forums/{forumId}/join") suspend fun join(@Path("forumId") forumId: String): ApiResp
  @POST("api/forums/{forumId}/leave") suspend fun leave(@Path("forumId") forumId: String): ApiResp

  @POST("api/forums/{forumId}/posts") suspend fun createPost(@Path("forumId") forumId: String, @Body body: CreatePostBody): ApiResp
  @GET("api/forums/{forumId}/posts") suspend fun listPosts(@Path("forumId") forumId: String, @Query("page") page: Int? = null, @Query("limit") limit: Int? = null): ApiResp

  @POST("api/forums/posts/{postId}/comments") suspend fun addComment(@Path("postId") postId: String, @Body body: AddCommentBody): ApiResp
  @GET("api/forums/posts/{postId}/comments") suspend fun listComments(@Path("postId") postId: String, @Query("parentCommentId") parent: String? = null, @Query("page") page: Int? = null, @Query("limit") limit: Int? = null): ApiResp

  @POST("api/forums/interactions/bulk") suspend fun bulk(@Body body: BulkBody): ApiResp
  @POST("api/forums/{forumId}/members/role") suspend fun changeRole(@Path("forumId") forumId: String, @Body body: RoleBody): ApiResp
}

// Optimistic batching helper
class ReactionsBuffer(private val api: ForumsApi) {
  private val buffer = mutableListOf<BulkOp>()
  private var job: Job? = null
  fun likePost(postId: String) { enqueue(BulkOp(op = "post_reaction", postId = postId, type = "like")) }
  fun dislikePost(postId: String) { enqueue(BulkOp(op = "post_reaction", postId = postId, type = "dislike")) }
  fun likeComment(commentId: String) { enqueue(BulkOp(op = "comment_reaction", commentId = commentId, type = "like")) }
  fun dislikeComment(commentId: String) { enqueue(BulkOp(op = "comment_reaction", commentId = commentId, type = "dislike")) }
  fun sharePost(postId: String) { enqueue(BulkOp(op = "post_share", postId = postId)) }
  private fun enqueue(op: BulkOp) {
    synchronized(buffer) { buffer += op }
    job?.cancel()
    job = CoroutineScope(Dispatchers.IO).launch {
      delay(350)
      val ops = synchronized(buffer) { buffer.toList().also { buffer.clear() } }
      if (ops.isNotEmpty()) api.bulk(BulkBody(ops))
    }
  }
}

## Validation & limits
- Auth required. IDs are 24-hex. Slugs are lowercase alnum + '-'.
- Post permission controls posting; commenting mirrors it.
- Rate limits apply to posts, comments, reactions.

## Errors
- 400 validation, 401 unauthorized, 403 forbidden, 404 not found, 429 rate limited.

## Tips
- Optimistic UI for reactions; debounce 250–500ms and batch via /interactions/bulk.
- Paginate posts/comments; treat content as plain text (server sanitizes HTML). Escape on render.
- Reconcile counters on refresh (aggregation runs periodically).

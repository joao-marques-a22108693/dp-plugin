package com.tfc.ulht.dpplugin.dplib

import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class DPClient {
    /* private val authenticator = object : Authenticator {
        var username: String? = null
        var token: String? = null

        override fun authenticate(route: Route?, response: Response): Request? {
            if (username == null || token == null || response.request.header("Authorization") != null) {
                return null
            }

            return response.request.newBuilder()
                .header("Authorization", Credentials.basic(username!!, token!!))
                .build()
        }
    }*/

    companion object {
        private val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    private var authString: String? = null

    val loggedIn: Boolean
        get() = authString != null

    var loggingIn: Boolean = false
        private set


    fun loginBlocking(token: String): Boolean {
        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/current")
            .header("Authorization", token)
            .build()

        return client.newCall(request).execute().let { response ->
            authString = if (response.isSuccessful) token else null

            response.isSuccessful.also {
                response.close()
            }
        }
    }

    fun login(token: String, callback: ((Boolean) -> Unit)?) {
        loggingIn = true

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/current")
            .header("Authorization", token)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loggingIn = false
                if (callback != null) callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                loggingIn = false
                authString = if (response.isSuccessful) token else null

                if (callback != null) callback(response.isSuccessful)
                response.close()
            }
        })
    }

    fun login(username: String, token: String, callback: ((Boolean) -> Unit)?) {
        val credentials = Credentials.basic(username, token)
        login(credentials, callback)
    }

    fun getAssigmentsBlocking(): List<Assignment>? {
        if (!loggedIn) return null

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/current")
            .header("Authorization", authString!!)
            .build()

        return client.newCall(request).execute().let { response ->
            try {
                val assignment = json.decodeFromString<List<Assignment>>(response.body!!.string())
                response.close()
                assignment
            } catch (_: Exception) {
                response.close()
                null
            }
        }
    }

    fun getAssignments(callback: (List<Assignment>?) -> Unit) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/current")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val assignment = json.decodeFromString<List<Assignment>>(response.body!!.string())
                    callback(assignment)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun getSubmissionsBlocking(assignmentId: String): List<SubmissionsResponse>? {
        if (!loggedIn) return null

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/$assignmentId/submissions")
            .header("Authorization", authString!!)
            .build()

        return client.newCall(request).execute().let { response ->
            try {
                val submissions = json.decodeFromString<List<SubmissionsResponse>>(response.body!!.string())
                response.close()
                submissions
            } catch (_: Exception) {
                response.close()
                null
            }
        }
    }

    fun getSubmissions(assignmentId: String, callback: ((List<SubmissionsResponse>?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/$assignmentId/submissions")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val submissions = json.decodeFromString<List<SubmissionsResponse>>(response.body!!.string())
                    callback(submissions)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun getGroupSubmissionsBlocking(assignmentId: String, groupId: Int): List<Submission>? {
        if (!loggedIn) return null

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/$assignmentId/submissions/$groupId")
            .header("Authorization", authString!!)
            .build()

        return client.newCall(request).execute().let { response ->
            try {
                val submissions = json.decodeFromString<List<Submission>>(response.body!!.string())
                response.close()
                submissions
            } catch (e: Exception) {
                response.close()
                null
            }
        }
    }

    fun getGroupSubmissions(assignmentId: String, groupId: Int, callback: ((List<Submission>?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignments/$assignmentId/submissions/$groupId")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val submissions = json.decodeFromString<List<Submission>>(response.body!!.string())
                    callback(submissions)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun downloadSubmissionBlocking(submissionId: String): InputStream? {
        if (!loggedIn) return null

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/download/$submissionId")
            .header("Authorization", authString!!)
            .build()

        return client.newCall(request).execute().let { response ->
            if (!response.isSuccessful) return@let null

            response.body?.byteStream().also {
                response.close()
            }
        }
    }

    fun downloadSubmission(submissionId: String, callback: ((InputStream?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/download/$submissionId")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) callback(null)

                callback(response.body?.byteStream())
                response.close()
            }
        })
    }

    fun getBuildReport(submissionId: String, callback: ((FullBuildReport?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/submissions/$submissionId")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val buildReport = json.decodeFromString<FullBuildReport>(response.body!!.string())
                    callback(buildReport)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun getStudentHistory(studentId: String, callback: ((StudentHistory?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/studentHistory/$studentId")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val studentHistory = json.decodeFromString<StudentHistory>(response.body!!.string())
                    callback(studentHistory)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun searchStudents(query: String, callback: ((List<StudentListResponse>?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/studentSearch/$query")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val studentHistory = json.decodeFromString<List<StudentListResponse>>(response.body!!.string())
                    callback(studentHistory)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun searchAssignments(query: String, callback: ((List<StudentListResponse>?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/assignmentSearch/$query")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    val assignments = json.decodeFromString<List<StudentListResponse>>(response.body!!.string())
                    callback(assignments)
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }

    fun markAsFinal(submissionId: String, callback: ((Boolean?) -> Unit)) {
        if (!loggedIn) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url(BASE_URL + "api/teacher/submissions/$submissionId/markAsFinal")
            .header("Authorization", authString!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    response.close()
                    return
                }

                try {
                    callback(response.body!!.string() == "true")
                    response.close()
                } catch (_: Exception) {
                    callback(null)
                    response.close()
                }
            }
        })
    }
}
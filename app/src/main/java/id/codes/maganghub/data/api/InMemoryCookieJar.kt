package id.codes.maganghub.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory thread-safe cookie jar for maintaining session cookies across
 * Kemnaker SSO OAuth redirects.
 */
class InMemoryCookieJar : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, CopyOnWriteArrayList<Cookie>>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val currentCookies = cookieStore.getOrPut(host) { CopyOnWriteArrayList() }

        for (newCookie in cookies) {
            // Remove existing cookie with identical name and path
            currentCookies.removeAll { it.name == newCookie.name && it.path == newCookie.path }
            currentCookies.add(newCookie)
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val validCookies = mutableListOf<Cookie>()
        val currentTime = System.currentTimeMillis()

        for ((domain, cookies) in cookieStore) {
            if (isDomainMatch(host, domain)) {
                val iterator = cookies.iterator()
                while (iterator.hasNext()) {
                    val cookie = iterator.next()
                    if (cookie.expiresAt < currentTime) {
                        cookies.remove(cookie)
                    } else if (cookie.matches(url)) {
                        validCookies.add(cookie)
                    }
                }
            }
        }
        return validCookies
    }

    private fun isDomainMatch(targetHost: String, cookieDomain: String): Boolean {
        if (targetHost.equals(cookieDomain, ignoreCase = true)) return true
        if (targetHost.endsWith(".$cookieDomain", ignoreCase = true)) return true
        if (cookieDomain.startsWith(".") && targetHost.endsWith(cookieDomain, ignoreCase = true)) return true
        return false
    }

    @Synchronized
    fun clear() {
        cookieStore.clear()
    }
}

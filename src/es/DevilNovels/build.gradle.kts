import io.github.keiyoushi.gradle.api.ContentWarning

plugins {
    alias(ns.plugins.extension)
}

keiyoushi {
    name = "DevilNovels"
    versionCode = 1
    contentWarning = ContentWarning.SAFE
    theme = "lightnovelwpnovel"

    source {
        lang = "es"
        baseUrl = "https://devilnovels.com"
    }

    deeplink {
        host("devilnovels.com")
        path("/..*")
    }
}
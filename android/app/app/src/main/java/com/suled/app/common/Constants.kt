package com.suled.app.common

/**
 * Application-wide constants.
 * Centralizes magic numbers and configuration values for better maintainability.
 */
object Constants {
    
    /**
     * Network configuration constants
     */
    object Network {
        /** Network connection timeout in seconds */
        const val CONNECT_TIMEOUT_SECONDS = 30L
        
        /** Network read timeout in seconds */
        const val READ_TIMEOUT_SECONDS = 30L
        
        /** Network write timeout in seconds */
        const val WRITE_TIMEOUT_SECONDS = 30L
    }
    
    /**
     * API query parameters and limits
     */
    object Api {
        /** Default maximum results for tournament queries */
        const val DEFAULT_MAX_RESULTS = 100
        
        /** Tournament list query limit for mobile screens */
        const val TOURNAMENT_LIST_LIMIT = 50
    }
    
    /**
     * Database configuration constants
     */
    object Database {
        /** Database name */
        const val DATABASE_NAME = "suled_database"
        
        /** Current database version */
        const val DATABASE_VERSION = 2
        
        /** Cache time-to-live in milliseconds (7 days) */
        const val CACHE_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000
        
        /** Cache time-to-live in days */
        const val CACHE_TTL_DAYS = 7
    }
    
    /**
     * Coroutine Flow configuration
     */
    object Flow {
        /** StateFlow subscription timeout in milliseconds (5 seconds) */
        const val STATE_FLOW_TIMEOUT_MILLIS = 5000L
    }
    
    /**
     * UI configuration constants
     */
    object Ui {
        /** Loading state minimum display duration to prevent flicker */
        const val MIN_LOADING_DURATION_MILLIS = 300L
    }
    
    /**
     * Date and time formats
     */
    object DateFormat {
        /** ISO 8601 date format (YYYY-MM-DD) */
        const val ISO_LOCAL_DATE = "yyyy-MM-dd"
        
        /** Display date format */
        const val DISPLAY_DATE = "MMM dd, yyyy"
        
        /** Display time format */
        const val DISPLAY_TIME = "HH:mm"
    }
}

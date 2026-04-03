package com.lumina.tvaggregator.data

import com.lumina.tvaggregator.data.model.Content

/**
 * Simple content navigation holder for passing content between screens
 * In a production app, this would be replaced with proper argument passing or a state store
 */
object ContentNavigation {
    private var selectedContent: Content? = null

    fun setSelectedContent(content: Content) {
        selectedContent = content
    }

    fun getSelectedContent(): Content? {
        return selectedContent
    }

    fun clearSelectedContent() {
        selectedContent = null
    }
}
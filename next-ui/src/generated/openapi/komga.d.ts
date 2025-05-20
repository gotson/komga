export interface paths {
    "/actuator/info": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get server information
         * @description Required role: **ADMIN**
         */
        get: operations["getActuatorInfo"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Logout
         * @description Invalidates the current session and clean up any remember-me authentication.
         */
        get: operations["postLogout"];
        put?: never;
        /**
         * Logout
         * @description Invalidates the current session and clean up any remember-me authentication.
         */
        post: operations["postLogout_1"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/age-ratings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List age ratings
         * @description Can be filtered by various criteria
         */
        get: operations["getAgeRatings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/announcements": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve announcements
         * @description Required role: **ADMIN**
         */
        get: operations["getAnnouncements"];
        /**
         * Mark announcements as read
         * @description Required role: **ADMIN**
         */
        put: operations["markAnnouncementsRead"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/authors": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List authors
         * @deprecated
         * @description Use GET /api/v2/authors instead. Deprecated since 1.20.0.
         */
        get: operations["getAuthorsDeprecated"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/authors/names": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List authors' names */
        get: operations["getAuthorsNames"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/authors/roles": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List authors' roles */
        get: operations["getAuthorsRoles"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List books
         * @deprecated
         * @description Use POST /api/v1/books/list instead. Deprecated since 1.19.0.
         */
        get: operations["getAllBooksDeprecated"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/duplicates": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List duplicate books
         * @description Return books that have the same file hash.
         *
         *     Required role: **ADMIN**
         */
        get: operations["getBooksDuplicates"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/import": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Import books
         * @description Required role: **ADMIN**
         */
        post: operations["importBooks"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/latest": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List latest books
         * @description Return newly added or updated books.
         */
        get: operations["getBooksLatest"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/list": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /** List books */
        post: operations["getBooks"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/metadata": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /**
         * Update book metadata in bulk
         * @description Set a field to null to unset the metadata. You can omit fields you don't want to update.
         *
         *     Required role: **ADMIN**
         */
        patch: operations["updateBookMetadataByBatch"];
        trace?: never;
    };
    "/api/v1/books/ondeck": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List books on deck
         * @description Return first unread book of series with at least one book read and no books in progress.
         */
        get: operations["getBooksOnDeck"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/thumbnails": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Regenerate books posters
         * @description Required role: **ADMIN**
         */
        put: operations["booksRegenerateThumbnails"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book details */
        get: operations["getBookById"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/analyze": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Analyze book
         * @description Required role: **ADMIN**
         */
        post: operations["bookAnalyze"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/file": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Download book file
         * @description Download the book file.
         *
         *     Required role: **FILE_DOWNLOAD**
         */
        get: operations["downloadBookFile"];
        put?: never;
        post?: never;
        /**
         * Delete book file
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteBookFile"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/file/*": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Download book file
         * @description Download the book file.
         *
         *     Required role: **FILE_DOWNLOAD**
         */
        get: operations["downloadBookFile_1"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/manifest": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book's WebPub manifest */
        get: operations["getBookWebPubManifest"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/manifest/divina": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book's WebPub manifest (DiViNa) */
        get: operations["getBookWebPubManifestDivina"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/manifest/epub": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book's WebPub manifest (Epub) */
        get: operations["getBookWebPubManifestEpub"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/manifest/pdf": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book's WebPub manifest (PDF) */
        get: operations["getBookWebPubManifestPdf"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/metadata": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /**
         * Update book metadata
         * @description Set a field to null to unset the metadata. You can omit fields you don't want to update.
         *
         *     Required role: **ADMIN**
         */
        patch: operations["updateBookMetadata"];
        trace?: never;
    };
    "/api/v1/books/{bookId}/metadata/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Refresh book metadata
         * @description Required role: **ADMIN**
         */
        post: operations["bookRefreshMetadata"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/next": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get next book in series */
        get: operations["getBookSiblingNext"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/pages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List book pages */
        get: operations["getBookPages"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/pages/{pageNumber}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get book page image
         * @description Required role: **PAGE_STREAMING**
         */
        get: operations["getBookPageByNumber"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/pages/{pageNumber}/raw": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get raw book page
         * @description Returns the book page in raw format, without content negotiation.
         *
         *     Required role: **PAGE_STREAMING**
         */
        get: operations["getBookPageRawByNumber"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/pages/{pageNumber}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get book page thumbnail
         * @description The image is resized to 300px on the largest dimension.
         */
        get: operations["getBookPageThumbnailByNumber"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/positions": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List book's positions
         * @description The Positions API is a proposed standard for OPDS 2 and Readium. It is used by the Epub Reader.
         */
        get: operations["getBookPositions"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/previous": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get previous book in series */
        get: operations["getBookSiblingPrevious"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/progression": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get book progression
         * @description The Progression API is a proposed standard for OPDS 2 and Readium. It is used by the Epub Reader.
         */
        get: operations["getBookProgression"];
        /**
         * Mark book progression
         * @description The Progression API is a proposed standard for OPDS 2 and Readium. It is used by the Epub Reader.
         */
        put: operations["updateBookProgression"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/read-progress": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Mark book as unread
         * @description Mark book as unread
         */
        delete: operations["deleteBookReadProgress"];
        options?: never;
        head?: never;
        /**
         * Mark book's read progress
         * @description Mark book as read and/or change page progress.
         */
        patch: operations["markBookReadProgress"];
        trace?: never;
    };
    "/api/v1/books/{bookId}/readlists": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List book's readlists */
        get: operations["getReadListsByBookId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/resource/{resource}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get Epub resource
         * @description Return a resource from within an Epub book.
         */
        get: operations["getBookEpubResource"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book's poster image */
        get: operations["getBookThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/thumbnails": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List book posters */
        get: operations["getBookThumbnails"];
        put?: never;
        /**
         * Add book poster
         * @description Required role: **ADMIN**
         */
        post: operations["addUserUploadedBookThumbnail"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/thumbnails/{thumbnailId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get book poster image */
        get: operations["getBookThumbnailById"];
        put?: never;
        post?: never;
        /**
         * Delete book poster
         * @description Only uploaded posters can be deleted.
         *
         *     Required role: **ADMIN**
         */
        delete: operations["deleteUserUploadedBookThumbnail"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/books/{bookId}/thumbnails/{thumbnailId}/selected": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Mark book poster as selected
         * @description Required role: **ADMIN**
         */
        put: operations["markBookThumbnailSelected"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/claim": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve claim status
         * @description Check whether this server has already been claimed.
         */
        get: operations["getClaimStatus"];
        put?: never;
        /**
         * Claim server
         * @description Creates an admin user with the provided credentials.
         */
        post: operations["claimServer"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/client-settings/global": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Delete global settings
         * @description Setting key should be a valid lowercase namespace string like 'application.domain.key'
         *
         *     Required role: **ADMIN**
         */
        delete: operations["deleteGlobalSettings"];
        options?: never;
        head?: never;
        /**
         * Save global settings
         * @description Setting key should be a valid lowercase namespace string like 'application.domain.key'
         *
         *     Required role: **ADMIN**
         */
        patch: operations["saveGlobalSetting"];
        trace?: never;
    };
    "/api/v1/client-settings/global/list": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve global client settings
         * @description For unauthenticated users, only settings with 'allowUnauthorized=true' will be returned.
         */
        get: operations["getGlobalSettings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/client-settings/user": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Delete user settings
         * @description Setting key should be a valid lowercase namespace string like 'application.domain.key'
         */
        delete: operations["deleteUserSettings"];
        options?: never;
        head?: never;
        /**
         * Save user settings
         * @description Setting key should be a valid lowercase namespace string like 'application.domain.key'
         */
        patch: operations["saveUserSetting"];
        trace?: never;
    };
    "/api/v1/client-settings/user/list": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Retrieve user client settings */
        get: operations["getUserSettings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List collections */
        get: operations["getCollections"];
        put?: never;
        /**
         * Create collection
         * @description Required role: **ADMIN**
         */
        post: operations["createCollection"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get collection details */
        get: operations["getCollectionById"];
        put?: never;
        post?: never;
        /**
         * Delete collection
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteCollectionById"];
        options?: never;
        head?: never;
        /**
         * Update collection
         * @description Required role: **ADMIN**
         */
        patch: operations["updateCollectionById"];
        trace?: never;
    };
    "/api/v1/collections/{id}/series": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List collection's series */
        get: operations["getSeriesByCollectionId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections/{id}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get collection's poster image */
        get: operations["getCollectionThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections/{id}/thumbnails": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List collection's posters */
        get: operations["getCollectionThumbnails"];
        put?: never;
        /**
         * Add collection poster
         * @description Required role: **ADMIN**
         */
        post: operations["addUserUploadedCollectionThumbnail"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections/{id}/thumbnails/{thumbnailId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get collection poster image */
        get: operations["getCollectionThumbnailById"];
        put?: never;
        post?: never;
        /**
         * Delete collection poster
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteUserUploadedCollectionThumbnail"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/collections/{id}/thumbnails/{thumbnailId}/selected": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Mark collection poster as selected
         * @description Required role: **ADMIN**
         */
        put: operations["markCollectionThumbnailSelected"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/filesystem": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Directory listing
         * @description List folders and files from the host server's file system. If no request body is passed then the root directories are returned.
         *
         *     Required role: **ADMIN**
         */
        post: operations["getDirectoryListing"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/fonts/families": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List font families
         * @description List all available font families.
         */
        get: operations["getFonts"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/fonts/resource/{fontFamily}/css": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Download CSS file
         * @description Download a CSS file with the @font-face block for the font family. This is used by the Epub Reader to change fonts.
         */
        get: operations["getFontFamilyAsCss"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/fonts/resource/{fontFamily}/{fontFile}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Download font file */
        get: operations["getFontFile"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/genres": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List genres
         * @description Can be filtered by various criteria
         */
        get: operations["getGenres"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/history": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List historical events
         * @description Required role: **ADMIN**
         */
        get: operations["getHistoricalEvents"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/languages": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List languages
         * @description Can be filtered by various criteria
         */
        get: operations["getLanguages"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/libraries": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List all libraries
         * @description The libraries are filtered based on the current user's permissions
         */
        get: operations["getLibraries"];
        put?: never;
        /**
         * Create a library
         * @description Required role: **ADMIN**
         */
        post: operations["addLibrary"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/libraries/{libraryId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get details for a single library */
        get: operations["getLibraryById"];
        /**
         * Update a library
         * @deprecated
         * @description Use PATCH /api/v1/libraries/{libraryId} instead. Deprecated since 1.3.0.
         *
         *     Required role: **ADMIN**
         */
        put: operations["updateLibraryByIdDeprecated"];
        post?: never;
        /**
         * Delete a library
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteLibraryById"];
        options?: never;
        head?: never;
        /**
         * Update a library
         * @description You can omit fields you don't want to update
         *
         *     Required role: **ADMIN**
         */
        patch: operations["updateLibraryById"];
        trace?: never;
    };
    "/api/v1/libraries/{libraryId}/analyze": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Analyze a library
         * @description Required role: **ADMIN**
         */
        post: operations["libraryAnalyze"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/libraries/{libraryId}/empty-trash": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Empty trash for a library
         * @description Required role: **ADMIN**
         */
        post: operations["libraryEmptyTrash"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/libraries/{libraryId}/metadata/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Refresh metadata for a library
         * @description Required role: **ADMIN**
         */
        post: operations["libraryRefreshMetadata"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/libraries/{libraryId}/scan": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Scan a library
         * @description Required role: **ADMIN**
         */
        post: operations["libraryScan"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/login/set-cookie": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Set cookie
         * @description Forcefully return Set-Cookie header, even if the session is contained in the X-Auth-Token header.
         */
        get: operations["convertHeaderSessionToCookie"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/oauth2/providers": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List registered OAuth2 providers */
        get: operations["getOAuth2Providers"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List known duplicates
         * @description Required role: **ADMIN**
         */
        get: operations["getKnownPageHashes"];
        /**
         * Mark duplicate page as known
         * @description Required role: **ADMIN**
         */
        put: operations["createOrUpdateKnownPageHash"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/unknown": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List unknown duplicates
         * @description Required role: **ADMIN**
         */
        get: operations["getUnknownPageHashes"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/unknown/{pageHash}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get unknown duplicate image thumbnail
         * @description Required role: **ADMIN**
         */
        get: operations["getUnknownPageHashThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/{pageHash}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List duplicate matches
         * @description Required role: **ADMIN**
         */
        get: operations["getPageHashMatches"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/{pageHash}/delete-all": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Delete all duplicate pages by hash
         * @description Required role: **ADMIN**
         */
        post: operations["deleteDuplicatePagesByPageHash"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/{pageHash}/delete-match": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Delete specific duplicate page
         * @description Required role: **ADMIN**
         */
        post: operations["deleteSingleMatchByPageHash"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/page-hashes/{pageHash}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get known duplicate image thumbnail
         * @description Required role: **ADMIN**
         */
        get: operations["getKnownPageHashThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/publishers": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List publishers
         * @description Can be filtered by various criteria
         */
        get: operations["getPublishers"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List readlists */
        get: operations["getReadLists"];
        put?: never;
        /**
         * Create readlist
         * @description Required role: **ADMIN**
         */
        post: operations["createReadList"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/match/comicrack": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Match ComicRack list
         * @description Required role: **ADMIN**
         */
        post: operations["matchComicRackList"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get readlist details */
        get: operations["getReadListById"];
        put?: never;
        post?: never;
        /**
         * Delete readlist
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteReadListById"];
        options?: never;
        head?: never;
        /**
         * Update readlist
         * @description Required role: **ADMIN**
         */
        patch: operations["updateReadListById"];
        trace?: never;
    };
    "/api/v1/readlists/{id}/books": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List readlist's books */
        get: operations["getBooksByReadListId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/books/{bookId}/next": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get next book in readlist */
        get: operations["getBookSiblingNextInReadList"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/books/{bookId}/previous": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get previous book in readlist */
        get: operations["getBookSiblingPreviousInReadList"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/file": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Download readlist
         * @description Download the whole readlist as a ZIP file.
         *
         *     Required role: **FILE_DOWNLOAD**
         */
        get: operations["downloadReadListAsZip"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/read-progress/tachiyomi": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get readlist read progress (Mihon)
         * @description Mihon specific, due to how read progress is handled in Mihon.
         */
        get: operations["getMihonReadProgressByReadListId"];
        /**
         * Update readlist read progress (Mihon)
         * @description Mihon specific, due to how read progress is handled in Mihon.
         */
        put: operations["updateMihonReadProgressByReadListId"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get readlist's poster image */
        get: operations["getReadListThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/thumbnails": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List readlist's posters */
        get: operations["getReadListThumbnails"];
        put?: never;
        /**
         * Add readlist poster
         * @description Required role: **ADMIN**
         */
        post: operations["addUserUploadedReadListThumbnail"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/thumbnails/{thumbnailId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get readlist poster image */
        get: operations["getReadListThumbnailById"];
        put?: never;
        post?: never;
        /**
         * Delete readlist poster
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteUserUploadedReadListThumbnail"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/readlists/{id}/thumbnails/{thumbnailId}/selected": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Mark readlist poster as selected
         * @description Required role: **ADMIN**
         */
        put: operations["markReadListThumbnailSelected"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/releases": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List releases
         * @description Required role: **ADMIN**
         */
        get: operations["getReleases"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List series
         * @deprecated
         * @description Use POST /api/v1/series/list instead. Deprecated since 1.19.0.
         */
        get: operations["getSeriesDeprecated"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/alphabetical-groups": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List series groups
         * @deprecated
         * @description Use POST /api/v1/series/list/alphabetical-groups instead. Deprecated since 1.19.0.
         */
        get: operations["getSeriesAlphabeticalGroupsDeprecated"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/latest": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List latest series
         * @description Return recently added or updated series.
         */
        get: operations["getSeriesLatest"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/list": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /** List series */
        post: operations["getSeries"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/list/alphabetical-groups": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * List series groups
         * @description List series grouped by the first character of their sort title.
         */
        post: operations["getSeriesAlphabeticalGroups"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/new": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List new series
         * @description Return newly added series.
         */
        get: operations["getSeriesNew"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/release-dates": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List series release dates
         * @description Can be filtered by various criteria
         */
        get: operations["getSeriesReleaseDates"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/updated": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List updated series
         * @description Return recently updated series, but not newly added ones.
         */
        get: operations["getSeriesUpdated"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get series details */
        get: operations["getSeriesById"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/analyze": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Analyze series
         * @description Required role: **ADMIN**
         */
        post: operations["seriesAnalyze"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/books": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List series' books
         * @deprecated
         * @description Use POST /api/v1/books/list instead. Deprecated since 1.19.0.
         */
        get: operations["getBooksBySeriesId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/collections": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List series' collections */
        get: operations["getCollectionsBySeriesId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/file": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Download series
         * @description Download the whole series as a ZIP file.
         *
         *     Required role: **FILE_DOWNLOAD**
         */
        get: operations["downloadSeriesAsZip"];
        put?: never;
        post?: never;
        /**
         * Delete series files
         * @description Delete all of the series' books files on disk.
         *
         *     Required role: **ADMIN**
         */
        delete: operations["deleteSeriesFile"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/metadata": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /**
         * Update series metadata
         * @description Required role: **ADMIN**
         */
        patch: operations["updateSeriesMetadata"];
        trace?: never;
    };
    "/api/v1/series/{seriesId}/metadata/refresh": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Refresh series metadata
         * @description Required role: **ADMIN**
         */
        post: operations["seriesRefreshMetadata"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/read-progress": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Mark series as read
         * @description Mark all book for series as read
         */
        post: operations["markSeriesAsRead"];
        /**
         * Mark series as unread
         * @description Mark all book for series as unread
         */
        delete: operations["markSeriesAsUnread"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/thumbnail": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get series' poster image */
        get: operations["getSeriesThumbnail"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/thumbnails": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** List series posters */
        get: operations["getSeriesThumbnails"];
        put?: never;
        /**
         * Add series poster
         * @description Required role: **ADMIN**
         */
        post: operations["addUserUploadedSeriesThumbnail"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/thumbnails/{thumbnailId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Get series poster image */
        get: operations["getSeriesThumbnailById"];
        put?: never;
        post?: never;
        /**
         * Delete series poster
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteUserUploadedSeriesThumbnail"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/series/{seriesId}/thumbnails/{thumbnailId}/selected": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        /**
         * Mark series poster as selected
         * @description Required role: **ADMIN**
         */
        put: operations["markSeriesThumbnailSelected"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/settings": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve server settings
         * @description Required role: **ADMIN**
         */
        get: operations["getServerSettings"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /**
         * Update server settings
         * @description You can omit fields you don't want to update
         *
         *     Required role: **ADMIN**
         */
        patch: operations["updateServerSettings"];
        trace?: never;
    };
    "/api/v1/sharing-labels": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List sharing labels
         * @description Can be filtered by various criteria
         */
        get: operations["getSharingLabels"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/syncpoints/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Delete all sync points
         * @description If an API Key ID is passed, deletes only the sync points associated with that API Key. Deleting sync points will allow a Kobo to sync from scratch upon the next sync.
         */
        delete: operations["deleteSyncPointsForCurrentUser"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/tags": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List tags
         * @description Can be filtered by various criteria
         */
        get: operations["getTags"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/tags/book": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List book tags
         * @description Can be filtered by various criteria
         */
        get: operations["getBookTags"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/tags/series": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List series tags
         * @description Can be filtered by various criteria
         */
        get: operations["getSeriesTags"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/tasks": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Clear task queue
         * @description Cancel all tasks queued
         *
         *     Required role: **ADMIN**
         */
        delete: operations["emptyTaskQueue"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/transient-books": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Scan folder for transient books
         * @description Scan provided folder for transient books.
         *
         *     Required role: **ADMIN**
         */
        post: operations["scanTransientBooks"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/transient-books/{id}/analyze": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * Analyze transient book
         * @description Required role: **ADMIN**
         */
        post: operations["analyzeTransientBook"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v1/transient-books/{id}/pages/{pageNumber}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get transient book page
         * @description Required role: **ADMIN**
         */
        get: operations["getPageByTransientBookId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/authors": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List authors
         * @description Can be filtered by various criteria
         */
        get: operations["getAuthors"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/series/{seriesId}/read-progress/tachiyomi": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Get series read progress (Mihon)
         * @description Mihon specific, due to how read progress is handled in Mihon.
         */
        get: operations["getMihonReadProgressBySeriesId"];
        /**
         * Update series read progress (Mihon)
         * @description Mihon specific, due to how read progress is handled in Mihon.
         */
        put: operations["updateMihonReadProgressBySeriesId"];
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * List users
         * @description Required role: **ADMIN**
         */
        get: operations["getUsers"];
        put?: never;
        /**
         * Create user
         * @description Required role: **ADMIN**
         */
        post: operations["addUser"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/authentication-activity": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve authentication activity
         * @description Required role: **ADMIN**
         */
        get: operations["getAuthenticationActivity"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/me": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Retrieve current user */
        get: operations["getCurrentUser"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/me/api-keys": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Retrieve API keys */
        get: operations["getApiKeysForCurrentUser"];
        put?: never;
        /** Create API key */
        post: operations["createApiKeyForCurrentUser"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/me/api-keys/{keyId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /** Delete API key */
        delete: operations["deleteApiKeyByKeyId"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/me/authentication-activity": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /** Retrieve authentication activity for the current user */
        get: operations["getAuthenticationActivityForCurrentUser"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/me/password": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /** Update current user's password */
        patch: operations["updatePasswordForCurrentUser"];
        trace?: never;
    };
    "/api/v2/users/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * Delete user
         * @description Required role: **ADMIN**
         */
        delete: operations["deleteUserById"];
        options?: never;
        head?: never;
        /**
         * Update user
         * @description Required role: **ADMIN**
         */
        patch: operations["updateUserById"];
        trace?: never;
    };
    "/api/v2/users/{id}/authentication-activity/latest": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * Retrieve latest authentication activity for a user
         * @description Required role: **ADMIN**
         */
        get: operations["getLatestAuthenticationActivityByUserId"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api/v2/users/{id}/password": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        /**
         * Update user's password
         * @description Required role: **ADMIN**
         */
        patch: operations["updatePasswordByUserId"];
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        After: {
            operator: "After";
        } & (Omit<components["schemas"]["Date"], "operator"> & {
            /** Format: date-time */
            dateTime: Date;
        });
        AgeRating: components["schemas"]["Series"] & {
            ageRating: components["schemas"]["GreaterThan"] | components["schemas"]["Is"] | components["schemas"]["IsNot"] | components["schemas"]["IsNotNullT"] | components["schemas"]["IsNullT"] | components["schemas"]["LessThan"];
        };
        AgeRestrictionDto: {
            /** Format: int32 */
            age: number;
            /** @enum {string} */
            restriction: "ALLOW_ONLY" | "EXCLUDE";
        };
        AgeRestrictionUpdateDto: {
            /** Format: int32 */
            age: number;
            /** @enum {string} */
            restriction: "ALLOW_ONLY" | "EXCLUDE";
        };
        AllOfBook: components["schemas"]["Book"] & {
            allOf: (components["schemas"]["AllOfBook"] | components["schemas"]["AnyOfBook"] | components["schemas"]["Author"] | components["schemas"]["Deleted"] | components["schemas"]["LibraryId"] | components["schemas"]["MediaProfile"] | components["schemas"]["MediaStatus"] | components["schemas"]["NumberSort"] | components["schemas"]["OneShot"] | components["schemas"]["Poster"] | components["schemas"]["ReadListId"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesId"] | components["schemas"]["Tag"] | components["schemas"]["Title"])[];
        };
        AllOfSeries: components["schemas"]["Series"] & {
            allOf: (components["schemas"]["AgeRating"] | components["schemas"]["AllOfSeries"] | components["schemas"]["AnyOfSeries"] | components["schemas"]["Author"] | components["schemas"]["CollectionId"] | components["schemas"]["Complete"] | components["schemas"]["Deleted"] | components["schemas"]["Genre"] | components["schemas"]["Language"] | components["schemas"]["LibraryId"] | components["schemas"]["OneShot"] | components["schemas"]["Publisher"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesStatus"] | components["schemas"]["SharingLabel"] | components["schemas"]["Tag"] | components["schemas"]["Title"] | components["schemas"]["TitleSort"])[];
        };
        AlternateTitleDto: {
            label: string;
            title: string;
        };
        AlternateTitleUpdateDto: {
            label: string;
            title: string;
        };
        AnyOfBook: components["schemas"]["Book"] & {
            anyOf: (components["schemas"]["AllOfBook"] | components["schemas"]["AnyOfBook"] | components["schemas"]["Author"] | components["schemas"]["Deleted"] | components["schemas"]["LibraryId"] | components["schemas"]["MediaProfile"] | components["schemas"]["MediaStatus"] | components["schemas"]["NumberSort"] | components["schemas"]["OneShot"] | components["schemas"]["Poster"] | components["schemas"]["ReadListId"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesId"] | components["schemas"]["Tag"] | components["schemas"]["Title"])[];
        };
        AnyOfSeries: components["schemas"]["Series"] & {
            anyOf: (components["schemas"]["AgeRating"] | components["schemas"]["AllOfSeries"] | components["schemas"]["AnyOfSeries"] | components["schemas"]["Author"] | components["schemas"]["CollectionId"] | components["schemas"]["Complete"] | components["schemas"]["Deleted"] | components["schemas"]["Genre"] | components["schemas"]["Language"] | components["schemas"]["LibraryId"] | components["schemas"]["OneShot"] | components["schemas"]["Publisher"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesStatus"] | components["schemas"]["SharingLabel"] | components["schemas"]["Tag"] | components["schemas"]["Title"] | components["schemas"]["TitleSort"])[];
        };
        ApiKeyDto: {
            comment: string;
            /** Format: date-time */
            createdDate: Date;
            id: string;
            key: string;
            /** Format: date-time */
            lastModifiedDate: Date;
            userId: string;
        };
        ApiKeyRequestDto: {
            comment: string;
        };
        AuthenticationActivityDto: {
            apiKeyComment?: string;
            apiKeyId?: string;
            /** Format: date-time */
            dateTime: Date;
            email?: string;
            error?: string;
            ip?: string;
            source?: string;
            success: boolean;
            userAgent?: string;
            userId?: string;
        };
        Author: components["schemas"]["Series"] & {
            author: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        } & components["schemas"]["Book"];
        AuthorDto: {
            name: string;
            role: string;
        };
        AuthorUpdateDto: {
            name: string;
            role: string;
        };
        Before: {
            operator: "Before";
        } & (Omit<components["schemas"]["Date"], "operator"> & {
            /** Format: date-time */
            dateTime: Date;
        });
        BeginsWith: {
            operator: "BeginsWith";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        Book: Record<string, never>;
        BookDto: {
            /** Format: date-time */
            created: Date;
            deleted: boolean;
            fileHash: string;
            /** Format: date-time */
            fileLastModified: Date;
            id: string;
            /** Format: date-time */
            lastModified: Date;
            libraryId: string;
            media: components["schemas"]["MediaDto"];
            metadata: components["schemas"]["BookMetadataDto"];
            name: string;
            /** Format: int32 */
            number: number;
            oneshot: boolean;
            readProgress?: components["schemas"]["ReadProgressDto"];
            seriesId: string;
            seriesTitle: string;
            size: string;
            /** Format: int64 */
            sizeBytes: number;
            url: string;
        };
        BookImportBatchDto: {
            books: components["schemas"]["BookImportDto"][];
            /** @enum {string} */
            copyMode: "MOVE" | "COPY" | "HARDLINK";
        };
        BookImportDto: {
            destinationName?: string;
            seriesId: string;
            sourceFile: string;
            upgradeBookId?: string;
        };
        BookMetadataAggregationDto: {
            authors: components["schemas"]["AuthorDto"][];
            /** Format: date-time */
            created: Date;
            /** Format: date-time */
            lastModified: Date;
            /** Format: date */
            releaseDate?: string;
            summary: string;
            summaryNumber: string;
            tags: string[];
        };
        BookMetadataDto: {
            authors: components["schemas"]["AuthorDto"][];
            authorsLock: boolean;
            /** Format: date-time */
            created: Date;
            isbn: string;
            isbnLock: boolean;
            /** Format: date-time */
            lastModified: Date;
            links: components["schemas"]["WebLinkDto"][];
            linksLock: boolean;
            number: string;
            numberLock: boolean;
            /** Format: float */
            numberSort: number;
            numberSortLock: boolean;
            /** Format: date */
            releaseDate?: string;
            releaseDateLock: boolean;
            summary: string;
            summaryLock: boolean;
            tags: string[];
            tagsLock: boolean;
            title: string;
            titleLock: boolean;
        };
        /** @description Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update. */
        BookMetadataUpdateDto: {
            authors?: components["schemas"]["AuthorUpdateDto"][];
            authorsLock?: boolean;
            isbn?: string;
            isbnLock?: boolean;
            links?: components["schemas"]["WebLinkUpdateDto"][];
            linksLock?: boolean;
            number?: string;
            numberLock?: boolean;
            /** Format: float */
            numberSort?: number;
            numberSortLock?: boolean;
            /** Format: date */
            releaseDate?: string;
            releaseDateLock?: boolean;
            summary?: string;
            summaryLock?: boolean;
            tags?: string[];
            tagsLock?: boolean;
            title?: string;
            titleLock?: boolean;
        };
        BookSearch: {
            condition?: components["schemas"]["AllOfBook"] | components["schemas"]["AnyOfBook"] | components["schemas"]["Author"] | components["schemas"]["Deleted"] | components["schemas"]["LibraryId"] | components["schemas"]["MediaProfile"] | components["schemas"]["MediaStatus"] | components["schemas"]["NumberSort"] | components["schemas"]["OneShot"] | components["schemas"]["Poster"] | components["schemas"]["ReadListId"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesId"] | components["schemas"]["Tag"] | components["schemas"]["Title"];
            fullTextSearch?: string;
        };
        Boolean: {
            operator: string;
        };
        ClaimStatus: {
            isClaimed: boolean;
        };
        ClientSettingDto: {
            allowUnauthorized?: boolean;
            value: string;
        };
        ClientSettingGlobalUpdateDto: {
            allowUnauthorized: boolean;
            value: string;
        };
        ClientSettingUserUpdateDto: {
            value: string;
        };
        CollectionCreationDto: {
            name: string;
            ordered: boolean;
            seriesIds: string[];
        };
        CollectionDto: {
            /** Format: date-time */
            createdDate: Date;
            filtered: boolean;
            id: string;
            /** Format: date-time */
            lastModifiedDate: Date;
            name: string;
            ordered: boolean;
            seriesIds: string[];
        };
        CollectionId: components["schemas"]["Series"] & {
            collectionId: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        CollectionUpdateDto: {
            name?: string;
            ordered?: boolean;
            seriesIds?: string[];
        };
        Complete: components["schemas"]["Series"] & {
            complete: components["schemas"]["IsFalse"] | components["schemas"]["IsTrue"];
        };
        Contains: {
            operator: "Contains";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        Date: {
            operator: string;
        };
        Deleted: components["schemas"]["Series"] & {
            deleted: components["schemas"]["IsFalse"] | components["schemas"]["IsTrue"];
        } & components["schemas"]["Book"];
        DirectoryListingDto: {
            directories: components["schemas"]["PathDto"][];
            files: components["schemas"]["PathDto"][];
            parent?: string;
        };
        DirectoryRequestDto: {
            path: string;
            showFiles: boolean;
        };
        DoesNotBeginWith: {
            operator: "DoesNotBeginWith";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        DoesNotContain: {
            operator: "DoesNotContain";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        DoesNotEndWith: {
            operator: "DoesNotEndWith";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        EndsWith: {
            operator: "EndsWith";
        } & (Omit<components["schemas"]["StringOp"], "operator"> & {
            value: string;
        });
        EqualityAuthorMatch: {
            operator: string;
        };
        EqualityMediaProfile: {
            operator: string;
        };
        EqualityNullableString: {
            operator: string;
        };
        EqualityPosterMatch: {
            operator: string;
        };
        EqualityReadStatus: {
            operator: string;
        };
        EqualityStatus: {
            operator: string;
        };
        EqualityString: {
            operator: string;
        };
        Genre: components["schemas"]["Series"] & {
            genre: components["schemas"]["Is"] | components["schemas"]["IsNot"] | components["schemas"]["IsNotNullT"] | components["schemas"]["IsNullT"];
        };
        GreaterThan: {
            operator: "GreaterThan";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & {
            value: Record<string, never>;
        } & Omit<components["schemas"]["NumericFloat"], "operator">);
        GroupCountDto: {
            /** Format: int32 */
            count: number;
            group: string;
        };
        HistoricalEventDto: {
            bookId?: string;
            properties: {
                [key: string]: string;
            };
            seriesId?: string;
            /** Format: date-time */
            timestamp: Date;
            type: string;
        };
        Is: {
            operator: "Is";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & {
            value: Record<string, never>;
        } & Omit<components["schemas"]["EqualityAuthorMatch"], "operator"> & Omit<components["schemas"]["EqualityString"], "operator"> & Omit<components["schemas"]["EqualityNullableString"], "operator"> & Omit<components["schemas"]["EqualityReadStatus"], "operator"> & Omit<components["schemas"]["EqualityStatus"], "operator"> & Omit<components["schemas"]["StringOp"], "operator"> & Omit<components["schemas"]["EqualityMediaProfile"], "operator"> & Omit<components["schemas"]["NumericFloat"], "operator"> & Omit<components["schemas"]["EqualityPosterMatch"], "operator">);
        IsFalse: {
            operator: "IsFalse";
        } & Omit<components["schemas"]["Boolean"], "operator">;
        IsInTheLast: {
            operator: "IsInTheLast";
        } & (Omit<components["schemas"]["Date"], "operator"> & {
            duration: {
                /** Format: int32 */
                nano?: number;
                negative?: boolean;
                positive?: boolean;
                /** Format: int64 */
                seconds?: number;
                units?: {
                    dateBased?: boolean;
                    durationEstimated?: boolean;
                    timeBased?: boolean;
                }[];
                zero?: boolean;
            };
        });
        IsNot: {
            operator: "IsNot";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & {
            value: Record<string, never>;
        } & Omit<components["schemas"]["EqualityAuthorMatch"], "operator"> & Omit<components["schemas"]["EqualityString"], "operator"> & Omit<components["schemas"]["EqualityNullableString"], "operator"> & Omit<components["schemas"]["EqualityReadStatus"], "operator"> & Omit<components["schemas"]["EqualityStatus"], "operator"> & Omit<components["schemas"]["StringOp"], "operator"> & Omit<components["schemas"]["EqualityMediaProfile"], "operator"> & Omit<components["schemas"]["NumericFloat"], "operator"> & Omit<components["schemas"]["EqualityPosterMatch"], "operator">);
        IsNotInTheLast: {
            operator: "IsNotInTheLast";
        } & (Omit<components["schemas"]["Date"], "operator"> & {
            duration: {
                /** Format: int32 */
                nano?: number;
                negative?: boolean;
                positive?: boolean;
                /** Format: int64 */
                seconds?: number;
                units?: {
                    dateBased?: boolean;
                    durationEstimated?: boolean;
                    timeBased?: boolean;
                }[];
                zero?: boolean;
            };
        });
        IsNotNull: {
            operator: "IsNotNull";
        } & Omit<components["schemas"]["Date"], "operator">;
        IsNotNullT: {
            operator: "IsNotNullT";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & Omit<components["schemas"]["EqualityNullableString"], "operator">);
        IsNull: {
            operator: "IsNull";
        } & Omit<components["schemas"]["Date"], "operator">;
        IsNullT: {
            operator: "IsNullT";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & Omit<components["schemas"]["EqualityNullableString"], "operator">);
        IsTrue: {
            operator: "IsTrue";
        } & Omit<components["schemas"]["Boolean"], "operator">;
        ItemDto: {
            _komga?: components["schemas"]["KomgaExtensionDto"];
            author?: components["schemas"]["AuthorDto"];
            content_html?: string;
            /** Format: date-time */
            date_modified?: Date;
            id: string;
            summary?: string;
            tags: string[];
            title?: string;
            url?: string;
        };
        JsonFeedDto: {
            description?: string;
            home_page_url?: string;
            items: components["schemas"]["ItemDto"][];
            title: string;
            version: string;
        };
        KomgaExtensionDto: {
            read: boolean;
        };
        Language: components["schemas"]["Series"] & {
            language: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        LessThan: {
            operator: "LessThan";
        } & (Omit<components["schemas"]["NumericNullableInteger"], "operator"> & {
            value: Record<string, never>;
        } & Omit<components["schemas"]["NumericFloat"], "operator">);
        LibraryCreationDto: {
            analyzeDimensions: boolean;
            convertToCbz: boolean;
            emptyTrashAfterScan: boolean;
            hashFiles: boolean;
            hashKoreader: boolean;
            hashPages: boolean;
            importBarcodeIsbn: boolean;
            importComicInfoBook: boolean;
            importComicInfoCollection: boolean;
            importComicInfoReadList: boolean;
            importComicInfoSeries: boolean;
            importComicInfoSeriesAppendVolume: boolean;
            importEpubBook: boolean;
            importEpubSeries: boolean;
            importLocalArtwork: boolean;
            importMylarSeries: boolean;
            name: string;
            oneshotsDirectory?: string;
            repairExtensions: boolean;
            root: string;
            scanCbx: boolean;
            scanDirectoryExclusions: string[];
            scanEpub: boolean;
            scanForceModifiedTime: boolean;
            /** @enum {string} */
            scanInterval: "DISABLED" | "HOURLY" | "EVERY_6H" | "EVERY_12H" | "DAILY" | "WEEKLY";
            scanOnStartup: boolean;
            scanPdf: boolean;
            /** @enum {string} */
            seriesCover: "FIRST" | "FIRST_UNREAD_OR_FIRST" | "FIRST_UNREAD_OR_LAST" | "LAST";
        };
        LibraryDto: {
            analyzeDimensions: boolean;
            convertToCbz: boolean;
            emptyTrashAfterScan: boolean;
            hashFiles: boolean;
            hashKoreader: boolean;
            hashPages: boolean;
            id: string;
            importBarcodeIsbn: boolean;
            importComicInfoBook: boolean;
            importComicInfoCollection: boolean;
            importComicInfoReadList: boolean;
            importComicInfoSeries: boolean;
            importComicInfoSeriesAppendVolume: boolean;
            importEpubBook: boolean;
            importEpubSeries: boolean;
            importLocalArtwork: boolean;
            importMylarSeries: boolean;
            name: string;
            oneshotsDirectory?: string;
            repairExtensions: boolean;
            root: string;
            scanCbx: boolean;
            scanDirectoryExclusions: string[];
            scanEpub: boolean;
            scanForceModifiedTime: boolean;
            /** @enum {string} */
            scanInterval: "DISABLED" | "HOURLY" | "EVERY_6H" | "EVERY_12H" | "DAILY" | "WEEKLY";
            scanOnStartup: boolean;
            scanPdf: boolean;
            /** @enum {string} */
            seriesCover: "FIRST" | "FIRST_UNREAD_OR_FIRST" | "FIRST_UNREAD_OR_LAST" | "LAST";
            unavailable: boolean;
        };
        LibraryId: components["schemas"]["Series"] & {
            libraryId: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        } & components["schemas"]["Book"];
        /** @description Fields to update. You can omit fields you don't want to update. */
        LibraryUpdateDto: {
            analyzeDimensions?: boolean;
            convertToCbz?: boolean;
            emptyTrashAfterScan?: boolean;
            hashFiles?: boolean;
            hashKoreader?: boolean;
            hashPages?: boolean;
            importBarcodeIsbn?: boolean;
            importComicInfoBook?: boolean;
            importComicInfoCollection?: boolean;
            importComicInfoReadList?: boolean;
            importComicInfoSeries?: boolean;
            importComicInfoSeriesAppendVolume?: boolean;
            importEpubBook?: boolean;
            importEpubSeries?: boolean;
            importLocalArtwork?: boolean;
            importMylarSeries?: boolean;
            name?: string;
            oneshotsDirectory?: string;
            repairExtensions?: boolean;
            root?: string;
            scanCbx?: boolean;
            scanDirectoryExclusions?: string[];
            scanEpub?: boolean;
            scanForceModifiedTime?: boolean;
            /** @enum {string} */
            scanInterval?: "DISABLED" | "HOURLY" | "EVERY_6H" | "EVERY_12H" | "DAILY" | "WEEKLY";
            scanOnStartup?: boolean;
            scanPdf?: boolean;
            /** @enum {string} */
            seriesCover?: "FIRST" | "FIRST_UNREAD_OR_FIRST" | "FIRST_UNREAD_OR_LAST" | "LAST";
        };
        Location: {
            fragments: string[];
            /** Format: int32 */
            position?: number;
            /** Format: float */
            progression?: number;
            /** Format: float */
            totalProgression?: number;
        };
        MediaDto: {
            comment: string;
            epubDivinaCompatible: boolean;
            epubIsKepub: boolean;
            mediaProfile: string;
            mediaType: string;
            /** Format: int32 */
            pagesCount: number;
            status: string;
        };
        MediaProfile: components["schemas"]["Book"] & {
            mediaProfile: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        MediaStatus: components["schemas"]["Book"] & {
            mediaStatus: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        MediaType: {
            charset?: string;
            concrete?: boolean;
            parameters?: {
                [key: string]: string;
            };
            /** Format: double */
            qualityValue?: number;
            subtype?: string;
            subtypeSuffix?: string;
            type?: string;
            wildcardSubtype?: boolean;
            wildcardType?: boolean;
        };
        NumberSort: components["schemas"]["Book"] & {
            numberSort: components["schemas"]["GreaterThan"] | components["schemas"]["Is"] | components["schemas"]["IsNot"] | components["schemas"]["LessThan"];
        };
        NumericFloat: {
            operator: string;
        };
        NumericNullableInteger: {
            operator: string;
        };
        OAuth2ClientDto: {
            name: string;
            registrationId: string;
        };
        OneShot: components["schemas"]["Series"] & {
            oneShot: components["schemas"]["IsFalse"] | components["schemas"]["IsTrue"];
        } & components["schemas"]["Book"];
        PageAuthenticationActivityDto: {
            content?: components["schemas"]["AuthenticationActivityDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageAuthorDto: {
            content?: components["schemas"]["AuthorDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageBookDto: {
            content?: components["schemas"]["BookDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageCollectionDto: {
            content?: components["schemas"]["CollectionDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageDto: {
            fileName: string;
            /** Format: int32 */
            height?: number;
            mediaType: string;
            /** Format: int32 */
            number: number;
            size: string;
            /** Format: int64 */
            sizeBytes?: number;
            /** Format: int32 */
            width?: number;
        };
        PageHashCreationDto: {
            /** @enum {string} */
            action: "DELETE_AUTO" | "DELETE_MANUAL" | "IGNORE";
            hash: string;
            /** Format: int64 */
            size?: number;
        };
        PageHashKnownDto: {
            /** @enum {string} */
            action: "DELETE_AUTO" | "DELETE_MANUAL" | "IGNORE";
            /** Format: date-time */
            created: Date;
            /** Format: int32 */
            deleteCount: number;
            hash: string;
            /** Format: date-time */
            lastModified: Date;
            /** Format: int32 */
            matchCount: number;
            /** Format: int64 */
            size?: number;
        };
        PageHashMatchDto: {
            bookId: string;
            fileName: string;
            /** Format: int64 */
            fileSize: number;
            mediaType: string;
            /** Format: int32 */
            pageNumber: number;
            url: string;
        };
        PageHashUnknownDto: {
            hash: string;
            /** Format: int32 */
            matchCount: number;
            /** Format: int64 */
            size?: number;
        };
        PageHistoricalEventDto: {
            content?: components["schemas"]["HistoricalEventDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PagePageHashKnownDto: {
            content?: components["schemas"]["PageHashKnownDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PagePageHashMatchDto: {
            content?: components["schemas"]["PageHashMatchDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PagePageHashUnknownDto: {
            content?: components["schemas"]["PageHashUnknownDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageReadListDto: {
            content?: components["schemas"]["ReadListDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageSeriesDto: {
            content?: components["schemas"]["SeriesDto"][];
            empty?: boolean;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            number?: number;
            /** Format: int32 */
            numberOfElements?: number;
            pageable?: components["schemas"]["PageableObject"];
            /** Format: int32 */
            size?: number;
            sort?: components["schemas"]["SortObject"];
            /** Format: int64 */
            totalElements?: number;
            /** Format: int32 */
            totalPages?: number;
        };
        PageableObject: {
            /** Format: int64 */
            offset?: number;
            /** Format: int32 */
            pageNumber?: number;
            /** Format: int32 */
            pageSize?: number;
            paged?: boolean;
            sort?: components["schemas"]["SortObject"];
            unpaged?: boolean;
        };
        PasswordUpdateDto: {
            password: string;
        };
        PathDto: {
            name: string;
            path: string;
            type: string;
        };
        Poster: components["schemas"]["Book"] & {
            poster: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        Publisher: components["schemas"]["Series"] & {
            publisher: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        R2Device: {
            id: string;
            name: string;
        };
        R2Locator: {
            href: string;
            koboSpan?: string;
            locations?: components["schemas"]["Location"];
            text?: components["schemas"]["Text"];
            title?: string;
            type: string;
        };
        R2Positions: {
            positions: components["schemas"]["R2Locator"][];
            /** Format: int32 */
            total: number;
        };
        R2Progression: {
            device: components["schemas"]["R2Device"];
            locator: components["schemas"]["R2Locator"];
            /** Format: date-time */
            modified: Date;
        };
        ReadListCreationDto: {
            bookIds: string[];
            name: string;
            ordered: boolean;
            summary: string;
        };
        ReadListDto: {
            bookIds: string[];
            /** Format: date-time */
            createdDate: Date;
            filtered: boolean;
            id: string;
            /** Format: date-time */
            lastModifiedDate: Date;
            name: string;
            ordered: boolean;
            summary: string;
        };
        ReadListId: components["schemas"]["Book"] & {
            readListId: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        ReadListMatchDto: {
            errorCode: string;
            name: string;
        };
        ReadListRequestBookDto: {
            number: string;
            series: string[];
        };
        ReadListRequestBookMatchBookDto: {
            bookId: string;
            number: string;
            title: string;
        };
        ReadListRequestBookMatchDto: {
            books: components["schemas"]["ReadListRequestBookMatchBookDto"][];
            series: components["schemas"]["ReadListRequestBookMatchSeriesDto"];
        };
        ReadListRequestBookMatchSeriesDto: {
            /** Format: date */
            releaseDate?: string;
            seriesId: string;
            title: string;
        };
        ReadListRequestBookMatchesDto: {
            matches: components["schemas"]["ReadListRequestBookMatchDto"][];
            request: components["schemas"]["ReadListRequestBookDto"];
        };
        ReadListRequestMatchDto: {
            errorCode: string;
            readListMatch: components["schemas"]["ReadListMatchDto"];
            requests: components["schemas"]["ReadListRequestBookMatchesDto"][];
        };
        ReadListUpdateDto: {
            bookIds?: string[];
            name?: string;
            ordered?: boolean;
            summary?: string;
        };
        ReadProgressDto: {
            completed: boolean;
            /** Format: date-time */
            created: Date;
            deviceId: string;
            deviceName: string;
            /** Format: date-time */
            lastModified: Date;
            /** Format: int32 */
            page: number;
            /** Format: date-time */
            readDate: Date;
        };
        /** @description page can be omitted if completed is set to true. completed can be omitted, and will be set accordingly depending on the page passed and the total number of pages in the book. */
        ReadProgressUpdateDto: {
            completed?: boolean;
            /** Format: int32 */
            page?: number;
        };
        ReadStatus: components["schemas"]["Series"] & {
            readStatus: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        } & components["schemas"]["Book"];
        ReleaseDate: components["schemas"]["Series"] & {
            releaseDate: components["schemas"]["After"] | components["schemas"]["Before"] | components["schemas"]["IsInTheLast"] | components["schemas"]["IsNotInTheLast"] | components["schemas"]["IsNotNull"] | components["schemas"]["IsNull"];
        } & components["schemas"]["Book"];
        ReleaseDto: {
            description: string;
            latest: boolean;
            preRelease: boolean;
            /** Format: date-time */
            releaseDate: Date;
            url: string;
            version: string;
        };
        ScanRequestDto: {
            path: string;
        };
        Series: Record<string, never>;
        SeriesDto: {
            /** Format: int32 */
            booksCount: number;
            /** Format: int32 */
            booksInProgressCount: number;
            booksMetadata: components["schemas"]["BookMetadataAggregationDto"];
            /** Format: int32 */
            booksReadCount: number;
            /** Format: int32 */
            booksUnreadCount: number;
            /** Format: date-time */
            created: Date;
            deleted: boolean;
            /** Format: date-time */
            fileLastModified: Date;
            id: string;
            /** Format: date-time */
            lastModified: Date;
            libraryId: string;
            metadata: components["schemas"]["SeriesMetadataDto"];
            name: string;
            oneshot: boolean;
            url: string;
        };
        SeriesId: components["schemas"]["Book"] & {
            seriesId: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        SeriesMetadataDto: {
            /** Format: int32 */
            ageRating?: number;
            ageRatingLock: boolean;
            alternateTitles: components["schemas"]["AlternateTitleDto"][];
            alternateTitlesLock: boolean;
            /** Format: date-time */
            created: Date;
            genres: string[];
            genresLock: boolean;
            language: string;
            languageLock: boolean;
            /** Format: date-time */
            lastModified: Date;
            links: components["schemas"]["WebLinkDto"][];
            linksLock: boolean;
            publisher: string;
            publisherLock: boolean;
            readingDirection: string;
            readingDirectionLock: boolean;
            sharingLabels: string[];
            sharingLabelsLock: boolean;
            status: string;
            statusLock: boolean;
            summary: string;
            summaryLock: boolean;
            tags: string[];
            tagsLock: boolean;
            title: string;
            titleLock: boolean;
            titleSort: string;
            titleSortLock: boolean;
            /** Format: int32 */
            totalBookCount?: number;
            totalBookCountLock: boolean;
        };
        /** @description Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update. */
        SeriesMetadataUpdateDto: {
            /** Format: int32 */
            ageRating?: number;
            ageRatingLock?: boolean;
            alternateTitles?: components["schemas"]["AlternateTitleUpdateDto"][];
            alternateTitlesLock?: boolean;
            genres?: string[];
            genresLock?: boolean;
            language?: string;
            languageLock?: boolean;
            links?: components["schemas"]["WebLinkUpdateDto"][];
            linksLock?: boolean;
            publisher?: string;
            publisherLock?: boolean;
            /** @enum {string} */
            readingDirection?: "LEFT_TO_RIGHT" | "RIGHT_TO_LEFT" | "VERTICAL" | "WEBTOON";
            readingDirectionLock?: boolean;
            sharingLabels?: string[];
            sharingLabelsLock?: boolean;
            /** @enum {string} */
            status?: "ENDED" | "ONGOING" | "ABANDONED" | "HIATUS";
            statusLock?: boolean;
            summary?: string;
            summaryLock?: boolean;
            tags?: string[];
            tagsLock?: boolean;
            title?: string;
            titleLock?: boolean;
            titleSort?: string;
            titleSortLock?: boolean;
            /** Format: int32 */
            totalBookCount?: number;
            totalBookCountLock?: boolean;
        };
        SeriesSearch: {
            condition?: components["schemas"]["AgeRating"] | components["schemas"]["AllOfSeries"] | components["schemas"]["AnyOfSeries"] | components["schemas"]["Author"] | components["schemas"]["CollectionId"] | components["schemas"]["Complete"] | components["schemas"]["Deleted"] | components["schemas"]["Genre"] | components["schemas"]["Language"] | components["schemas"]["LibraryId"] | components["schemas"]["OneShot"] | components["schemas"]["Publisher"] | components["schemas"]["ReadStatus"] | components["schemas"]["ReleaseDate"] | components["schemas"]["SeriesStatus"] | components["schemas"]["SharingLabel"] | components["schemas"]["Tag"] | components["schemas"]["Title"] | components["schemas"]["TitleSort"];
            fullTextSearch?: string;
        };
        SeriesStatus: components["schemas"]["Series"] & {
            seriesStatus: components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        SettingMultiSourceInteger: {
            /** Format: int32 */
            configurationSource: number;
            /** Format: int32 */
            databaseSource: number;
            /** Format: int32 */
            effectiveValue: number;
        };
        SettingMultiSourceString: {
            configurationSource: string;
            databaseSource: string;
            effectiveValue: string;
        };
        SettingsDto: {
            deleteEmptyCollections: boolean;
            deleteEmptyReadLists: boolean;
            kepubifyPath: components["schemas"]["SettingMultiSourceString"];
            /** Format: int32 */
            koboPort?: number;
            koboProxy: boolean;
            /** Format: int64 */
            rememberMeDurationDays: number;
            serverContextPath: components["schemas"]["SettingMultiSourceString"];
            serverPort: components["schemas"]["SettingMultiSourceInteger"];
            /** Format: int32 */
            taskPoolSize: number;
            /** @enum {string} */
            thumbnailSize: "DEFAULT" | "MEDIUM" | "LARGE" | "XLARGE";
        };
        /** @description Fields to update. You can omit fields you don't want to update. */
        SettingsUpdateDto: {
            deleteEmptyCollections?: boolean;
            deleteEmptyReadLists?: boolean;
            kepubifyPath?: string;
            /** Format: int32 */
            koboPort?: number;
            koboProxy?: boolean;
            /** Format: int64 */
            rememberMeDurationDays?: number;
            renewRememberMeKey?: boolean;
            serverContextPath?: string;
            /** Format: int32 */
            serverPort?: number;
            /** Format: int32 */
            taskPoolSize?: number;
            /** @enum {string} */
            thumbnailSize?: "DEFAULT" | "MEDIUM" | "LARGE" | "XLARGE";
        };
        SharedLibrariesUpdateDto: {
            all: boolean;
            libraryIds: string[];
        };
        SharingLabel: components["schemas"]["Series"] & {
            sharingLabel: components["schemas"]["Is"] | components["schemas"]["IsNot"] | components["schemas"]["IsNotNullT"] | components["schemas"]["IsNullT"];
        };
        SortObject: {
            empty?: boolean;
            sorted?: boolean;
            unsorted?: boolean;
        };
        StreamingResponseBody: Record<string, never>;
        StringOp: {
            operator: string;
        };
        TachiyomiReadProgressDto: {
            /** Format: int32 */
            booksCount: number;
            /** Format: int32 */
            booksInProgressCount: number;
            /** Format: int32 */
            booksReadCount: number;
            /** Format: int32 */
            booksUnreadCount: number;
            /** Format: int32 */
            lastReadContinuousIndex: number;
        };
        TachiyomiReadProgressUpdateDto: {
            /** Format: int32 */
            lastBookRead: number;
        };
        TachiyomiReadProgressUpdateV2Dto: {
            /** Format: float */
            lastBookNumberSortRead: number;
        };
        TachiyomiReadProgressV2Dto: {
            /** Format: int32 */
            booksCount: number;
            /** Format: int32 */
            booksInProgressCount: number;
            /** Format: int32 */
            booksReadCount: number;
            /** Format: int32 */
            booksUnreadCount: number;
            /** Format: float */
            lastReadContinuousNumberSort: number;
            /** Format: float */
            maxNumberSort: number;
        };
        Tag: components["schemas"]["Series"] & {
            tag: components["schemas"]["Is"] | components["schemas"]["IsNot"] | components["schemas"]["IsNotNullT"] | components["schemas"]["IsNullT"];
        } & components["schemas"]["Book"];
        Text: {
            after?: string;
            before?: string;
            highlight?: string;
        };
        ThumbnailBookDto: {
            bookId: string;
            /** Format: int64 */
            fileSize: number;
            /** Format: int32 */
            height: number;
            id: string;
            mediaType: string;
            selected: boolean;
            type: string;
            /** Format: int32 */
            width: number;
        };
        ThumbnailReadListDto: {
            /** Format: int64 */
            fileSize: number;
            /** Format: int32 */
            height: number;
            id: string;
            mediaType: string;
            readListId: string;
            selected: boolean;
            type: string;
            /** Format: int32 */
            width: number;
        };
        ThumbnailSeriesCollectionDto: {
            collectionId: string;
            /** Format: int64 */
            fileSize: number;
            /** Format: int32 */
            height: number;
            id: string;
            mediaType: string;
            selected: boolean;
            type: string;
            /** Format: int32 */
            width: number;
        };
        ThumbnailSeriesDto: {
            /** Format: int64 */
            fileSize: number;
            /** Format: int32 */
            height: number;
            id: string;
            mediaType: string;
            selected: boolean;
            seriesId: string;
            type: string;
            /** Format: int32 */
            width: number;
        };
        Title: components["schemas"]["Series"] & {
            title: components["schemas"]["BeginsWith"] | components["schemas"]["Contains"] | components["schemas"]["DoesNotBeginWith"] | components["schemas"]["DoesNotContain"] | components["schemas"]["DoesNotEndWith"] | components["schemas"]["EndsWith"] | components["schemas"]["Is"] | components["schemas"]["IsNot"];
        } & components["schemas"]["Book"];
        TitleSort: components["schemas"]["Series"] & {
            titleSort: components["schemas"]["BeginsWith"] | components["schemas"]["Contains"] | components["schemas"]["DoesNotBeginWith"] | components["schemas"]["DoesNotContain"] | components["schemas"]["DoesNotEndWith"] | components["schemas"]["EndsWith"] | components["schemas"]["Is"] | components["schemas"]["IsNot"];
        };
        TransientBookDto: {
            comment: string;
            /** Format: date-time */
            fileLastModified: Date;
            files: string[];
            id: string;
            mediaType: string;
            name: string;
            /** Format: float */
            number?: number;
            pages: components["schemas"]["PageDto"][];
            seriesId?: string;
            size: string;
            /** Format: int64 */
            sizeBytes: number;
            status: string;
            url: string;
        };
        UserCreationDto: {
            email: string;
            password: string;
            roles: string[];
        };
        UserDto: {
            ageRestriction?: components["schemas"]["AgeRestrictionDto"];
            email: string;
            id: string;
            labelsAllow: string[];
            labelsExclude: string[];
            roles: string[];
            sharedAllLibraries: boolean;
            sharedLibrariesIds: string[];
        };
        UserUpdateDto: {
            ageRestriction?: components["schemas"]["AgeRestrictionUpdateDto"];
            labelsAllow?: string[];
            labelsExclude?: string[];
            roles?: string[];
            sharedLibraries?: components["schemas"]["SharedLibrariesUpdateDto"];
        };
        ValidationErrorResponse: {
            violations: components["schemas"]["Violation"][];
        };
        Violation: {
            fieldName?: string;
            message?: string;
        };
        WPBelongsToDto: {
            collection: components["schemas"]["WPContributorDto"][];
            series: components["schemas"]["WPContributorDto"][];
        };
        WPContributorDto: {
            links: components["schemas"]["WPLinkDto"][];
            name: string;
            /** Format: float */
            position?: number;
        };
        WPLinkDto: {
            /** Format: int32 */
            height?: number;
            href?: string;
            properties: {
                [key: string]: {
                    [key: string]: Record<string, never>;
                };
            };
            rel?: string;
            templated?: boolean;
            title?: string;
            type?: string;
            /** Format: int32 */
            width?: number;
        };
        WPMetadataDto: {
            artist: string[];
            author: string[];
            belongsTo?: components["schemas"]["WPBelongsToDto"];
            colorist: string[];
            conformsTo?: string;
            contributor: string[];
            description?: string;
            editor: string[];
            identifier?: string;
            illustrator: string[];
            inker: string[];
            language?: string;
            letterer: string[];
            /** Format: date-time */
            modified?: Date;
            /** Format: int32 */
            numberOfPages?: number;
            penciler: string[];
            /** Format: date */
            published?: string;
            publisher: string[];
            /** @enum {string} */
            readingProgression?: "rtl" | "ltr" | "ttb" | "btt" | "auto";
            rendition: {
                [key: string]: Record<string, never>;
            };
            sortAs?: string;
            subject: string[];
            subtitle?: string;
            title: string;
            translator: string[];
            type?: string;
        };
        WPPublicationDto: {
            context?: string;
            images: components["schemas"]["WPLinkDto"][];
            landmarks: components["schemas"]["WPLinkDto"][];
            links: components["schemas"]["WPLinkDto"][];
            metadata: components["schemas"]["WPMetadataDto"];
            pageList: components["schemas"]["WPLinkDto"][];
            readingOrder: components["schemas"]["WPLinkDto"][];
            resources: components["schemas"]["WPLinkDto"][];
            toc: components["schemas"]["WPLinkDto"][];
        };
        WebLinkDto: {
            label: string;
            url: string;
        };
        WebLinkUpdateDto: {
            label: string;
            url?: string;
        };
    };
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    getActuatorInfo: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": unknown;
                };
            };
        };
    };
    postLogout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    postLogout_1: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
        };
    };
    getAgeRatings: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAnnouncements: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["JsonFeedDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markAnnouncementsRead: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": string[];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthorsDeprecated: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string;
                collection_id?: string;
                series_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["AuthorDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthorsNames: {
        parameters: {
            query?: {
                search?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthorsRoles: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAllBooksDeprecated: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string[];
                media_status?: ("UNKNOWN" | "ERROR" | "READY" | "UNSUPPORTED" | "OUTDATED")[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                released_after?: string;
                tag?: string[];
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooksDuplicates: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    importBooks: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BookImportBatchDto"];
            };
        };
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooksLatest: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooks: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BookSearch"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateBookMetadataByBatch: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": {
                    [key: string]: components["schemas"]["BookMetadataUpdateDto"];
                };
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooksOnDeck: {
        parameters: {
            query?: {
                library_id?: string[];
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    booksRegenerateThumbnails: {
        parameters: {
            query?: {
                for_bigger_result_only?: boolean;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["BookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    bookAnalyze: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    downloadBookFile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["StreamingResponseBody"];
                    "application/octet-stream": components["schemas"]["StreamingResponseBody"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteBookFile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    downloadBookFile_1: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["StreamingResponseBody"];
                    "application/octet-stream": components["schemas"]["StreamingResponseBody"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookWebPubManifest: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/divina+json": components["schemas"]["WPPublicationDto"];
                    "application/json": components["schemas"]["WPPublicationDto"];
                    "application/webpub+json": components["schemas"]["WPPublicationDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookWebPubManifestDivina: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/divina+json": components["schemas"]["WPPublicationDto"];
                    "application/json": components["schemas"]["WPPublicationDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookWebPubManifestEpub: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["WPPublicationDto"];
                    "application/webpub+json": components["schemas"]["WPPublicationDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookWebPubManifestPdf: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["WPPublicationDto"];
                    "application/webpub+json": components["schemas"]["WPPublicationDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateBookMetadata: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["BookMetadataUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    bookRefreshMetadata: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookSiblingNext: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["BookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookPages: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookPageByNumber: {
        parameters: {
            query?: {
                /** @description Convert the image to the provided format. */
                convert?: "jpeg" | "png";
                /** @description If set to true, pages will start at index 0. If set to false, pages will start at index 1. */
                zero_based?: boolean;
                contentNegotiation?: boolean;
            };
            header?: {
                /** @description Some very limited server driven content negotiation is handled. If a book is a PDF book, and the Accept header contains 'application/pdf' as a more specific type than other 'image/' types, a raw PDF page will be returned. */
                Accept?: components["schemas"]["MediaType"][];
            };
            path: {
                bookId: string;
                pageNumber: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "image/*": string;
                };
            };
        };
    };
    getBookPageRawByNumber: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                pageNumber: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                    "application/json": string;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookPageThumbnailByNumber: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                pageNumber: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getBookPositions: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["R2Positions"];
                    "application/vnd.readium.position-list+json": components["schemas"]["R2Positions"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookSiblingPrevious: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["BookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookProgression: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["R2Progression"];
                    "application/vnd.readium.progression+json": components["schemas"]["R2Progression"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateBookProgression: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["R2Progression"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteBookReadProgress: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markBookReadProgress: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReadProgressUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReadListsByBookId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ReadListDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookEpubResource: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                resource: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                    "application/json": string;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getBookThumbnails: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailBookDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addUserUploadedBookThumbnail: {
        parameters: {
            query?: {
                selected?: boolean;
            };
            header?: never;
            path: {
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookThumbnailById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    deleteUserUploadedBookThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markBookThumbnailSelected: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                bookId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getClaimStatus: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ClaimStatus"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    claimServer: {
        parameters: {
            query?: never;
            header: {
                "X-Komga-Email": string;
                "X-Komga-Password": string;
            };
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["UserDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteGlobalSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                /** @example [
                 *       "application.key1",
                 *       "application.key2"
                 *     ] */
                "application/json": string[];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    saveGlobalSetting: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                /** @example {
                 *       "application.key1": {
                 *         "value": "a string value",
                 *         "allowUnauthorized": true
                 *       },
                 *       "application.key2": {
                 *         "value": "{\"json\":\"object\"}",
                 *         "allowUnauthorized": false
                 *       }
                 *     } */
                "application/json": {
                    [key: string]: components["schemas"]["ClientSettingGlobalUpdateDto"];
                };
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getGlobalSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        [key: string]: components["schemas"]["ClientSettingDto"];
                    };
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteUserSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                /** @example [
                 *       "application.key1",
                 *       "application.key2"
                 *     ] */
                "application/json": string[];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    saveUserSetting: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                /** @example {
                 *       "application.key1": {
                 *         "value": "a string value"
                 *       },
                 *       "application.key2": {
                 *         "value": "{\"json\":\"object\"}"
                 *       }
                 *     } */
                "application/json": {
                    [key: string]: components["schemas"]["ClientSettingUserUpdateDto"];
                };
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getUserSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": {
                        [key: string]: components["schemas"]["ClientSettingDto"];
                    };
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCollections: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string[];
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageCollectionDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    createCollection: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CollectionCreationDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["CollectionDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCollectionById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["CollectionDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteCollectionById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateCollectionById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CollectionUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesByCollectionId: {
        parameters: {
            query?: {
                library_id?: string[];
                status?: ("ENDED" | "ONGOING" | "ABANDONED" | "HIATUS")[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                publisher?: string[];
                language?: string[];
                genre?: string[];
                tag?: string[];
                age_rating?: string[];
                release_year?: string[];
                deleted?: boolean;
                complete?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Author criteria in the format: name,role. Multiple author criteria are supported. */
                author?: string[];
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCollectionThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getCollectionThumbnails: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailSeriesCollectionDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addUserUploadedCollectionThumbnail: {
        parameters: {
            query?: {
                selected?: boolean;
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailSeriesCollectionDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCollectionThumbnailById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    deleteUserUploadedCollectionThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markCollectionThumbnailSelected: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getDirectoryListing: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: {
            content: {
                "application/json": components["schemas"]["DirectoryRequestDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["DirectoryListingDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getFonts: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getFontFamilyAsCss: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                fontFamily: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "text/css": string;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getFontFile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                fontFamily: string;
                fontFile: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getGenres: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getHistoricalEvents: {
        parameters: {
            query?: {
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageHistoricalEventDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getLanguages: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getLibraries: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["LibraryDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addLibrary: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LibraryCreationDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["LibraryDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getLibraryById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["LibraryDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateLibraryByIdDeprecated: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LibraryUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteLibraryById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateLibraryById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["LibraryUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    libraryAnalyze: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    libraryEmptyTrash: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    libraryRefreshMetadata: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    libraryScan: {
        parameters: {
            query?: {
                deep?: boolean;
            };
            header?: never;
            path: {
                libraryId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    convertHeaderSessionToCookie: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getOAuth2Providers: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["OAuth2ClientDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getKnownPageHashes: {
        parameters: {
            query?: {
                action?: ("DELETE_AUTO" | "DELETE_MANUAL" | "IGNORE")[];
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PagePageHashKnownDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    createOrUpdateKnownPageHash: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PageHashCreationDto"];
            };
        };
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getUnknownPageHashes: {
        parameters: {
            query?: {
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PagePageHashUnknownDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getUnknownPageHashThumbnail: {
        parameters: {
            query?: {
                resize?: number;
            };
            header?: never;
            path: {
                pageHash: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getPageHashMatches: {
        parameters: {
            query?: {
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path: {
                pageHash: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PagePageHashMatchDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteDuplicatePagesByPageHash: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                pageHash: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteSingleMatchByPageHash: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                pageHash: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PageHashMatchDto"];
            };
        };
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getKnownPageHashThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                pageHash: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getPublishers: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReadLists: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string[];
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageReadListDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    createReadList: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReadListCreationDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ReadListDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    matchComicRackList: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: {
            content: {
                "application/json": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ReadListRequestMatchDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReadListById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ReadListDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteReadListById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateReadListById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ReadListUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooksByReadListId: {
        parameters: {
            query?: {
                library_id?: string[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                tag?: string[];
                media_status?: ("UNKNOWN" | "ERROR" | "READY" | "UNSUPPORTED" | "OUTDATED")[];
                deleted?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Author criteria in the format: name,role. Multiple author criteria are supported. */
                author?: string[];
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookSiblingNextInReadList: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["BookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookSiblingPreviousInReadList: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                bookId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["BookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    downloadReadListAsZip: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["StreamingResponseBody"];
                    "application/octet-stream": components["schemas"]["StreamingResponseBody"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getMihonReadProgressByReadListId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["TachiyomiReadProgressDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateMihonReadProgressByReadListId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["TachiyomiReadProgressUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReadListThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getReadListThumbnails: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailReadListDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addUserUploadedReadListThumbnail: {
        parameters: {
            query?: {
                selected?: boolean;
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailReadListDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReadListThumbnailById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    deleteUserUploadedReadListThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markReadListThumbnailSelected: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getReleases: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ReleaseDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesDeprecated: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string[];
                collection_id?: string[];
                status?: ("ENDED" | "ONGOING" | "ABANDONED" | "HIATUS")[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                publisher?: string[];
                language?: string[];
                genre?: string[];
                tag?: string[];
                age_rating?: string[];
                release_year?: string[];
                sharing_label?: string[];
                deleted?: boolean;
                complete?: boolean;
                oneshot?: boolean;
                unpaged?: boolean;
                /** @description Search by regex criteria, in the form: regex,field. Supported fields are TITLE and TITLE_SORT. */
                search_regex?: string;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
                /** @description Author criteria in the format: name,role. Multiple author criteria are supported. */
                author?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesAlphabeticalGroupsDeprecated: {
        parameters: {
            query?: {
                search?: string;
                library_id?: string[];
                collection_id?: string[];
                status?: ("ENDED" | "ONGOING" | "ABANDONED" | "HIATUS")[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                publisher?: string[];
                language?: string[];
                genre?: string[];
                tag?: string[];
                age_rating?: string[];
                release_year?: string[];
                sharing_label?: string[];
                deleted?: boolean;
                complete?: boolean;
                oneshot?: boolean;
                /** @description Search by regex criteria, in the form: regex,field. Supported fields are TITLE and TITLE_SORT. */
                search_regex?: string;
                /** @description Author criteria in the format: name,role. Multiple author criteria are supported. */
                author?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["GroupCountDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesLatest: {
        parameters: {
            query?: {
                library_id?: string[];
                deleted?: boolean;
                oneshot?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeries: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SeriesSearch"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesAlphabeticalGroups: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SeriesSearch"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["GroupCountDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesNew: {
        parameters: {
            query?: {
                library_id?: string[];
                deleted?: boolean;
                oneshot?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesReleaseDates: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesUpdated: {
        parameters: {
            query?: {
                library_id?: string[];
                deleted?: boolean;
                oneshot?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["SeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    seriesAnalyze: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBooksBySeriesId: {
        parameters: {
            query?: {
                media_status?: ("UNKNOWN" | "ERROR" | "READY" | "UNSUPPORTED" | "OUTDATED")[];
                read_status?: ("UNREAD" | "READ" | "IN_PROGRESS")[];
                tag?: string[];
                deleted?: boolean;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
                /** @description Author criteria in the format: name,role. Multiple author criteria are supported. */
                author?: string[];
            };
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCollectionsBySeriesId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["CollectionDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    downloadSeriesAsZip: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["StreamingResponseBody"];
                    "application/octet-stream": components["schemas"]["StreamingResponseBody"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteSeriesFile: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateSeriesMetadata: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SeriesMetadataUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    seriesRefreshMetadata: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markSeriesAsRead: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markSeriesAsUnread: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    getSeriesThumbnails: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailSeriesDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addUserUploadedSeriesThumbnail: {
        parameters: {
            query?: {
                selected?: boolean;
            };
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: {
            content: {
                "multipart/form-data": {
                    /** Format: binary */
                    file: string;
                };
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ThumbnailSeriesDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesThumbnailById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
            /** @description default response */
            default: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string;
                    "image/jpeg": string;
                };
            };
        };
    };
    deleteUserUploadedSeriesThumbnail: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    markSeriesThumbnailSelected: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
                thumbnailId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description Accepted */
            202: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getServerSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["SettingsDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateServerSettings: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["SettingsUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSharingLabels: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteSyncPointsForCurrentUser: {
        parameters: {
            query?: {
                key_id?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getTags: {
        parameters: {
            query?: {
                library_id?: string[];
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getBookTags: {
        parameters: {
            query?: {
                series_id?: string;
                readlist_id?: string;
                library_id?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getSeriesTags: {
        parameters: {
            query?: {
                library_id?: string;
                collection_id?: string;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": string[];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    emptyTaskQueue: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": number;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    scanTransientBooks: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ScanRequestDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["TransientBookDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    analyzeTransientBook: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["TransientBookDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getPageByTransientBookId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
                pageNumber: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": string;
                    "application/json": string;
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthors: {
        parameters: {
            query?: {
                search?: string;
                role?: string;
                library_id?: string[];
                collection_id?: string;
                series_id?: string;
                readlist_id?: string;
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageAuthorDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getMihonReadProgressBySeriesId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["TachiyomiReadProgressV2Dto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateMihonReadProgressBySeriesId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                seriesId: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["TachiyomiReadProgressUpdateV2Dto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getUsers: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["UserDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    addUser: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UserCreationDto"];
            };
        };
        responses: {
            /** @description Created */
            201: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["UserDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthenticationActivity: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageAuthenticationActivityDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getCurrentUser: {
        parameters: {
            query?: {
                "remember-me"?: boolean;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["UserDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getApiKeysForCurrentUser: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ApiKeyDto"][];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    createApiKeyForCurrentUser: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["ApiKeyRequestDto"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["ApiKeyDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteApiKeyByKeyId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                keyId: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getAuthenticationActivityForCurrentUser: {
        parameters: {
            query?: {
                unpaged?: boolean;
                /** @description Zero-based page index (0..N) */
                page?: number;
                /** @description The size of the page to be returned */
                size?: number;
                /** @description Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
                sort?: string[];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["PageAuthenticationActivityDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updatePasswordForCurrentUser: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PasswordUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    deleteUserById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updateUserById: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["UserUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    getLatestAuthenticationActivityByUserId: {
        parameters: {
            query?: {
                apikey_id?: string;
            };
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["AuthenticationActivityDto"];
                };
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
    updatePasswordByUserId: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: string;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PasswordUpdateDto"];
            };
        };
        responses: {
            /** @description No Content */
            204: {
                headers: {
                    [name: string]: unknown;
                };
                content?: never;
            };
            /** @description Bad Request */
            400: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "*/*": components["schemas"]["ValidationErrorResponse"];
                };
            };
        };
    };
}

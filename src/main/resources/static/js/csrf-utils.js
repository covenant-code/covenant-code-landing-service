// csrf-utils.js (очищенный)
class CsrfUtils {
    constructor() {
        this.token = null;
        this.headerName = null;
        this.init();
    }

    init() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        if (csrfMeta && csrfHeaderMeta) {
            this.token = csrfMeta.getAttribute('content');
            this.headerName = csrfHeaderMeta.getAttribute('content');
        }
    }

    addCsrfToHeaders(headers = {}) {
        if (this.token && this.headerName) {
            return {
                ...headers,
                [this.headerName]: this.token
            };
        }
        return headers;
    }
}

window.csrfUtils = new CsrfUtils();
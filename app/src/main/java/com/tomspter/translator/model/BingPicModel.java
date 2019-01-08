package com.tomspter.translator.model;

import java.util.List;

public class BingPicModel {

    private List<Images> images;

    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
    }

    public class Images{

        private String urlBase;

        private String copyRightLink;

        public String getUrlBase() {
            return urlBase;
        }

        public void setUrlBase(String urlBase) {
            this.urlBase = urlBase;
        }

        public String getCopyRightLink() {
            return copyRightLink;
        }

        public void setCopyRightLink(String copyRightLink) {
            this.copyRightLink = copyRightLink;
        }
    }
}

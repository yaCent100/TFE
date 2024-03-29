package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Photo;

import java.util.List;

public interface PhotoServiceI {

    List<Photo> getAllPhotos();
    Photo getPhotoById(Long id);
    Photo savePhoto(Photo photo);
    Photo updatePhoto(Long id, Photo photo);
    void deletePhoto(Long id);
}

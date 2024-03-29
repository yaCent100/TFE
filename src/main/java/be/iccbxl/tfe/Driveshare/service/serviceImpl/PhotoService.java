package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Photo;
import be.iccbxl.tfe.Driveshare.repository.PhotoRepository;
import be.iccbxl.tfe.Driveshare.service.PhotoServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PhotoService implements PhotoServiceI {

    @Autowired
    private PhotoRepository photoRepository;

    @Override
    public List<Photo> getAllPhotos() {
        List<Photo> photos = new ArrayList<>();
        photoRepository.findAll().forEach(photos::add);
        return photos;       }

    @Override
    public Photo getPhotoById(Long id) {
        Optional<Photo> optionalPhoto = photoRepository.findById(id);
        return optionalPhoto.orElse(null);
    }

    @Override
    public Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    @Override
    public Photo updatePhoto(Long id, Photo newPhoto) {
        Optional<Photo> optionalPhoto = photoRepository.findById(id);
        if (optionalPhoto.isPresent()) {
            Photo existingPhoto = optionalPhoto.get();
            existingPhoto.setUrl(newPhoto.getUrl());
            return photoRepository.save(existingPhoto);
        }
        return null;
    }

    @Override
    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }
}

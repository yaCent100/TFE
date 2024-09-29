package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Photo;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.PhotoRepository;
import be.iccbxl.tfe.Driveshare.service.PhotoServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PhotoService implements PhotoServiceI {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private FileStorageService fileStorageService;

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




    @Transactional
    public boolean deletePhotoFromCar(Long carId, String photoUrl) {
        try {
            // Supprimer physiquement le fichier
            fileStorageService.deleteFile("photo-car", photoUrl);
            System.out.println("Fichier supprimé: " + photoUrl);

            // Supprimer la photo de la base de données
            photoRepository.deletePhotoByCarIdAndUrl(carId, photoUrl);
            System.out.println("Photo supprimée de la base de données: " + photoUrl);

            return true; // La suppression a réussi
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression de la photo ou du fichier: " + e.getMessage());
            return false; // La suppression a échoué
        }
    }

}

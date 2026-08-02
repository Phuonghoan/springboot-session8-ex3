package org.example.librarymanagement.service;

import org.example.librarymanagement.dto.BookCreateDTO;
import org.example.librarymanagement.dto.BookUpdateStockDTO;
import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.exception.FileStorageException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final BookRepository bookRepository;

    private final Path uploadDirectory =
            Paths.get("uploads")
                    .toAbsolutePath()
                    .normalize();

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Book createBook(BookCreateDTO dto) {
        MultipartFile coverImage = dto.getCoverImage();

        validateImage(coverImage);

        String storedFileName =
                generateStoredFileName(coverImage);

        Path targetPath = uploadDirectory
                .resolve(storedFileName)
                .normalize();

        if (!targetPath.startsWith(uploadDirectory)) {
            throw new FileStorageException(
                    "Đường dẫn lưu file không hợp lệ"
            );
        }

        try {
            Files.createDirectories(uploadDirectory);

            coverImage.transferTo(targetPath);

            Book book = new Book();
            book.setTitle(dto.getTitle().trim());
            book.setAuthor(dto.getAuthor().trim());
            book.setStock(dto.getStock());

            book.setCoverUrl(
                    "/uploads/" + storedFileName
            );

            try {
                return bookRepository.saveAndFlush(book);
            } catch (RuntimeException exception) {
                // Nếu lưu database thất bại thì xóa file vừa lưu
                Files.deleteIfExists(targetPath);
                throw exception;
            }

        } catch (IOException exception) {
            throw new FileStorageException(
                    "Không thể lưu ảnh bìa",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book with id "
                                        + id
                                        + " not found"
                        )
                );
    }

    @Transactional
    public Book updateBook(
            Long id,
            BookUpdateStockDTO dto
    ) {
        Book book = getBookById(id);

        book.setStock(dto.getStock());

        return bookRepository.save(book);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ảnh bìa không được để trống"
            );
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên file không hợp lệ"
            );
        }

        String extension = StringUtils
                .getFilenameExtension(originalFileName);

        if (extension == null) {
            throw new IllegalArgumentException(
                    "File không có phần mở rộng"
            );
        }

        String normalizedExtension =
                extension.toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(
                normalizedExtension
        )) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ ảnh JPG, JPEG, PNG, WEBP hoặc GIF"
            );
        }

        String contentType = file.getContentType();

        if (contentType != null
                && !contentType.startsWith("image/")
                && !contentType.equals(
                "application/octet-stream"
        )) {
            throw new IllegalArgumentException(
                    "File tải lên phải là hình ảnh"
            );
        }
    }

    private String generateStoredFileName(
            MultipartFile file
    ) {
        String extension = StringUtils
                .getFilenameExtension(
                        file.getOriginalFilename()
                );

        return UUID.randomUUID()
                + "."
                + extension.toLowerCase(Locale.ROOT);
    }
}

# Hướng dẫn Push Docker Images lên GitHub Container Registry

## Tổng quan

Hiện tại, dự án Komga fork sử dụng JReleaser để tự động build và push Docker images lên GitHub Container Registry (`ghcr.io/simplesoft-duongdt3/komga`) khi có release. Tài liệu này hướng dẫn cách để bạn tự push images lên repository của riêng bạn trên GitHub Container Registry.

Có hai phương pháp:
1. **Phương pháp thủ công**: Build image, tag và push bằng lệnh Docker trực tiếp lên GitHub Container Registry.
2. **Phương pháp sử dụng JReleaser**: Cấu hình JReleaser để push tự động lên GitHub Container Registry.

## Phương pháp thủ công

### Bước 1: Build Docker image

Sử dụng script `build-local-docker.sh` để build image local với version hiện tại (lấy từ `gradle.properties`):

```bash
./build-local-docker.sh
```

Script này sẽ build image với tag `komga-local:<version>`. Ví dụ: `komga-local:1.24.3`.

Nếu bạn muốn build với Dockerfile tối ưu (mặc định), script sử dụng `Dockerfile.local.optimized`. Bạn cũng có thể build trực tiếp bằng lệnh docker:

```bash
docker build -f Dockerfile.local.optimized -t komga-local:$(grep 'version=' gradle.properties | cut -d'=' -f2) .
```

### Bước 2: Tag image với repository đích

Để push lên GitHub Container Registry (thay `simplesoft-duongdt3` bằng tên tổ chức hoặc username GitHub):

```bash
docker tag komga-local:<version> ghcr.io/simplesoft-duongdt3/komga:<version>
docker tag komga-local:<version> ghcr.io/simplesoft-duongdt3/komga:latest
```

### Bước 3: Đăng nhập vào registry

**GitHub Container Registry:**
```bash
echo $GHCR_TOKEN | docker login ghcr.io -u simplesoft-duongdt3 --password-stdin
```

Trong đó `GHCR_TOKEN` là GitHub Personal Access Token với quyền `write:packages`. Bạn có thể tạo token tại https://github.com/settings/tokens.

### Bước 4: Push images

**Push lên GitHub Container Registry:**
```bash
docker push simplesoft-duongdt3/komga:<version>
docker push simplesoft-duongdt3/komga:latest
```

**Push lên GitHub Container Registry:**
```bash
docker push ghcr.io/simplesoft-duongdt3/komga:<version>
docker push ghcr.io/simplesoft-duongdt3/komga:latest
```

### Bước 5: Kiểm tra

Kiểm tra images đã được push lên GitHub Container Registry hoặc GHCR qua giao diện web hoặc lệnh:

```bash
docker pull simplesoft-duongdt3/komga:latest
```

## Phương pháp sử dụng JReleaser

JReleaser đã được cấu hình sẵn trong `build.gradle.kts` để build multi‑arch images (linux/amd64, linux/arm/v7, linux/arm64/v8) và push lên GitHub Container Registry và GHCR. Bạn có thể sửa cấu hình để thay đổi repository đích.

### Cấu hình JReleaser

Mở file `build.gradle.kts` và tìm phần `packagers.docker`. Sửa các mục sau:

1. **Thay đổi image names:** Tìm khối `imageNames` và thay thế bằng tên repository của bạn:

```kotlin
imageNames =
  listOf(
    "simplesoft-duongdt3/komga:latest",
    "simplesoft-duongdt3/komga:{{projectVersion}}",
    "simplesoft-duongdt3/komga:{{projectVersionMajor}}.x",
  )
```

Để push cùng lúc cả GitHub Container Registry và GHCR, bạn có thể thêm nhiều image names hoặc cấu hình thêm registry.

2. **Cấu hình registries:** Phần `registries` đã định nghĩa `ghcr.io` và `ghcr.io`. Nếu bạn muốn push lên GitHub Container Registry với username khác, bạn cần đảm bảo đăng nhập qua `docker login` trước khi chạy JReleaser. JReleaser sử dụng `externalLogin = true`, nghĩa là nó sẽ sử dụng thông tin đăng nhập từ Docker CLI.

Nếu bạn chỉ muốn push lên một registry, bạn có thể xóa registry không cần thiết.

3. **Label source:** Nếu muốn thay đổi label `org.opencontainers.image.source` trong image, bạn cần sửa file template `komga/docker/Dockerfile.tpl` (dòng cuối).

### Chạy JReleaser publish

Sau khi cấu hình, bạn có thể chạy lệnh sau để build và push images:

```bash
./gradlew jreleaserPublish
```

Lệnh này sẽ:
- Build multi‑arch images sử dụng Docker Buildx.
- Push images lên các registry đã cấu hình.

**Lưu ý:** JReleaser publish thường được kích hoạt trong workflow release khi `docker_release: true`. Bạn có thể chạy local nhưng cần đảm bảo đã login vào registry tương ứng.

### Cấu hình qua environment variables

JReleaser cho phép override một số cấu hình qua biến môi trường, ví dụ:

- `JRELEASER_DOCKER_IMAGENAMES`: danh sách image names phân tách bằng dấu phẩy.
- `JRELEASER_DOCKER_REGISTRIES`: danh sách registries.

Tuy nhiên, cách đơn giản nhất là sửa file `build.gradle.kts`.

## Lưu ý quan trọng

1. **Version:** Version được lấy từ `gradle.properties`. Đảm bảo version đúng trước khi push.
2. **Multi‑arch:** Nếu bạn muốn hỗ trợ nhiều kiến trúc (amd64, arm64, arm/v7), hãy sử dụng JReleaser hoặc Docker Buildx. Phương pháp thủ công chỉ build image cho kiến trúc hiện tại.
3. **Quyền riêng tư:** Repository trên GitHub Container Registry và GHCR có thể là public hoặc private. Với private repository, bạn cần đăng nhập với token có quyền push.
4. **Dung lượng:** Image size khoảng 200‑300MB. Đảm bảo bạn có đủ quota trên registry.

## Troubleshooting

- **Lỗi unauthorized:** Kiểm tra lại đăng nhập với `docker login`. Với GHCR, token cần có quyền `write:packages`.
- **Lỗi denied: requested access to the resource is denied:** Đảm bảo tên repository đúng định dạng `username/reponame` và bạn có quyền push.
- **Lỗi manifest invalid:** Khi push multi‑arch images, cần sử dụng Docker Buildx. JReleaser đã tích hợp sẵn.
- **Lỗi không tìm thấy Dockerfile:** Khi dùng JReleaser, template Dockerfile nằm ở `komga/docker/Dockerfile.tpl`. Đảm bảo file tồn tại.

## Tham khảo

- [Docker Documentation](https://docs.docker.com/)
- [GitHub Container Registry Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [JReleaser Docker Packager](https://jreleaser.org/guide/latest/packagers/docker.html)
from django.conf.urls import include
from django.urls import path

from .views import UserViewSet, PostViewSet, CommentViewSet, LikeViewSet


from rest_framework import routers

likeview = LikeViewSet.as_view({
    "get": "list",
    "post": "create",
    "delete": "destroy"
})

router = routers.DefaultRouter()
router.register(r'user', UserViewSet, basename='user')
router.register(r'user/(?P<deviceID>\w+)', UserViewSet, basename='user')
router.register(r'post/(?P<postID>\d+)/comment',
                CommentViewSet, basename='comment')
router.register(r'post/(?P<author>\w+)', PostViewSet, basename='post')
router.register(r'post', PostViewSet, basename='post')

urlpatterns = [
        path('', include(router.urls)),
        path('post/<int:postID>/like/', likeview),
]

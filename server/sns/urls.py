from django.conf.urls import include
from django.urls import path

from .views import UserViewSet

from rest_framework import routers

router = routers.DefaultRouter()
router.register('user', UserViewSet, basename='user')

urlpatterns = [
        path('', include(router.urls)),
]

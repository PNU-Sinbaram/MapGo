"""MapGo_server URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf.urls import url, include
from django.contrib import admin
from django.urls import path

from rest_framework import routers

from checkin.views import CheckinViewSet
from recommend.views import STCViewSet

router = routers.DefaultRouter()
router.register('Mapgo/checkin', CheckinViewSet, basename="checkin")

router2 = routers.DefaultRouter()
router2.register('Mapgo/recommend', STCViewSet, basename='recommend')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include(router.urls)),
    path('Mapgo/checkin/<str:userid>/',
         CheckinViewSet.as_view({'delete': 'delete'})),
    path('', include(router2.urls)),
]

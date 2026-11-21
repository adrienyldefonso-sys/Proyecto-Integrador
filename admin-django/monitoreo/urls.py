from django.urls import path
from . import views
from .views_alertas import AlertasView
urlpatterns = [
    path("parcelas/", views.ParcelaListView.as_view(), name="parcelas"),
    path(
        "parcelas/<str:parcela_id>/historial/",
        views.HistorialParcelaView.as_view(),
        name="historial-parcela",
    ),
    path("alertas/", AlertasView.as_view(), name="alertas"),
]
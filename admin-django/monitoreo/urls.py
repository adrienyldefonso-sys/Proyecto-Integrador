from django.urls import path
from . import views
from .views_alertas import AlertasView
from .views_revision import RevisarDiagnosticoView, RevisionesListView, SesionView
urlpatterns = [
    path("parcelas/", views.ParcelaListView.as_view(), name="parcelas"),
    path(
        "parcelas/<str:parcela_id>/historial/",
        views.HistorialParcelaView.as_view(),
        name="historial-parcela",
    ),
    path("alertas/", AlertasView.as_view(), name="alertas"),
    path("sesion/", SesionView.as_view(), name="sesion"),
    path("revisiones/", RevisionesListView.as_view(), name="revisiones"),
    path(
        "diagnosticos/<str:diagnostico_id>/revision/",
        RevisarDiagnosticoView.as_view(),
        name="revisar-diagnostico",
    ),
]
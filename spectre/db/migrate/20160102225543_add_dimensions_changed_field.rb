class AddDimensionsChangedField < ActiveRecord::Migration
  def change
    add_column :tests, :dimensions_changed, :boolean
  end
end
